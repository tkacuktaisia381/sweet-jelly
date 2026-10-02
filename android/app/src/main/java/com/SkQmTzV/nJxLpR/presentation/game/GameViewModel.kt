package com.SkQmTzV.nJxLpR.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.SkQmTzV.nJxLpR.core.config.GameConfig
import com.SkQmTzV.nJxLpR.domain.model.BoardState
import com.SkQmTzV.nJxLpR.domain.model.Cell
import com.SkQmTzV.nJxLpR.domain.model.JellyColor
import com.SkQmTzV.nJxLpR.domain.model.RoundResult
import com.SkQmTzV.nJxLpR.domain.model.Swap
import com.SkQmTzV.nJxLpR.domain.repository.LevelRepository
import com.SkQmTzV.nJxLpR.domain.usecase.ApplySwapUseCase
import com.SkQmTzV.nJxLpR.domain.usecase.BuildBoardUseCase
import com.SkQmTzV.nJxLpR.domain.usecase.EvaluateConnectionsUseCase
import com.SkQmTzV.nJxLpR.domain.usecase.SaveProgressUseCase
import com.SkQmTzV.nJxLpR.domain.usecase.ScoreRoundUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(
    levelId: Int,
    private val levelRepository: LevelRepository,
    private val applySwap: ApplySwapUseCase,
    private val evaluate: EvaluateConnectionsUseCase,
    private val scoreRound: ScoreRoundUseCase,
    private val saveProgress: SaveProgressUseCase,
    buildBoard: BuildBoardUseCase
) : ViewModel() {

    private val level = levelRepository.level(levelId)
    private val built = buildBoard(level)
    private val history = ArrayDeque(built.scrambleSwaps)

    private var board: BoardState = built.start
    private var status = GameStatus.PLAYING
    private var movesUsed = 0
    private var hintsUsed = 0
    private var selected: Cell? = null
    private var highlight: Set<Cell> = emptySet()
    private var inputLocked = false

    private var assistJob: Job? = null
    private var unlockJob: Job? = null
    private var endJob: Job? = null

    private val mutableState = MutableStateFlow(snapshot())
    val state: StateFlow<GameUiState> = mutableState.asStateFlow()

    private val mutableEvents = MutableSharedFlow<GameEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<GameEvent> = mutableEvents.asSharedFlow()

    private val mutableResult = MutableStateFlow<RoundResult?>(null)
    val result: StateFlow<RoundResult?> = mutableResult.asStateFlow()

    init {
        startAssist()
    }

    fun onTileTap(cell: Cell) {
        if (status != GameStatus.PLAYING || inputLocked) return
        if (!board.contains(cell)) return

        if (board.isFixed(cell)) {
            emit(GameEvent.Rejected(cell))
            return
        }

        val anchor = selected
        if (anchor == null || anchor == cell) {
            selected = if (anchor == cell) null else cell
            publish()
            return
        }

        if (!anchor.isNeighbourOf(cell)) {
            selected = cell
            publish()
            return
        }

        val swap = Swap(anchor, cell)
        if (!applySwap.isLegal(board, swap)) {
            emit(GameEvent.Rejected(cell))
            selected = null
            publish()
            return
        }

        selected = null
        commit(swap, remember = true)
    }

    fun onHint() {
        if (status != GameStatus.PLAYING || hintsLeft() <= 0) return
        val next = history.lastOrNull()
        hintsUsed++
        movesUsed++
        if (next != null) {
            highlight = setOf(next.a, next.b)
            emit(GameEvent.Hinted(highlight))
        }
        publish()
        resolveEnd()
    }

    fun onRestart() {
        endJob?.cancel()
        endJob = null
        board = built.start
        history.clear()
        history.addAll(built.scrambleSwaps)
        movesUsed = 0
        hintsUsed = 0
        selected = null
        highlight = emptySet()
        inputLocked = false
        status = GameStatus.PLAYING
        publish()
        emit(GameEvent.Restarted)
    }

    private fun commit(swap: Swap, remember: Boolean) {
        if (remember) history.addLast(swap)
        board = applySwap(board, swap)
        movesUsed++
        val linked = evaluate(board)
        board = board.withLinked(linked)
        highlight = emptySet()
        emit(GameEvent.Swapped(swap))
        lockInput()
        publish()
        if (linked.size == JellyColor.GOALS.size) {
            for (colour in JellyColor.GOALS) {
                val path = evaluate.pathOf(board, colour)
                if (path.isNotEmpty()) emit(GameEvent.Linked(path))
            }
        }
        resolveEnd()
    }

    private fun resolveEnd() {
        if (status != GameStatus.PLAYING) return
        val linked = board.linked
        if (linked.size == JellyColor.GOALS.size) {
            status = GameStatus.WON
            publish()
            scheduleFinish(true, GameConfig.WIN_HOLD_MS)
            return
        }
        if (movesUsed >= level.moveLimit) {
            status = GameStatus.LOST
            publish()
            scheduleFinish(false, GameConfig.LOSE_HOLD_MS)
        }
    }

    private fun scheduleFinish(win: Boolean, hold: Long) {
        assistJob?.cancel()
        assistJob = null
        endJob?.cancel()
        endJob = viewModelScope.launch {
            delay(hold)
            finish(win)
        }
    }

    private fun finish(win: Boolean) {
        val linkedCount = board.linked.size
        val movesLeft = if (level.moveLimit - movesUsed > 0) level.moveLimit - movesUsed else 0
        val score = scoreRound.score(linkedCount, movesLeft, win)
        val stars = scoreRound.stars(win, movesLeft, level.moveLimit, hintsUsed)
        val outcome = RoundResult(
            levelId = level.id,
            levelName = level.name,
            win = win,
            score = score,
            stars = stars,
            movesLeft = movesLeft,
            linkedCount = linkedCount,
            hasNext = level.id < levelRepository.count(),
            finalGrid = board.grid
        )
        saveProgress(outcome, levelRepository.count())
        mutableResult.value = outcome
    }

    private fun startAssist() {
        assistJob?.cancel()
        assistJob = viewModelScope.launch {
            delay(GameConfig.ASSIST_IDLE_MS)
            while (isActive && status == GameStatus.PLAYING) {
                assistStep()
                delay(GameConfig.ASSIST_STEP_MS)
            }
        }
    }

    private fun assistStep() {
        if (status != GameStatus.PLAYING) return
        val swap = history.removeLastOrNull() ?: return
        if (!applySwap.isLegal(board, swap)) return
        selected = null
        highlight = setOf(swap.a, swap.b)
        emit(GameEvent.Hinted(highlight))
        commit(swap, remember = false)
    }

    private fun lockInput() {
        inputLocked = true
        unlockJob?.cancel()
        unlockJob = viewModelScope.launch {
            delay(GameConfig.SWAP_ANIM_MS)
            inputLocked = false
        }
    }

    private fun hintsLeft(): Int {
        val left = GameConfig.HINTS_PER_LEVEL - hintsUsed
        return if (left > 0) left else 0
    }

    private fun emit(event: GameEvent) {
        mutableEvents.tryEmit(event)
    }

    private fun publish() {
        mutableState.value = snapshot()
    }

    private fun snapshot(): GameUiState = GameUiState(
        status = status,
        board = board,
        levelId = level.id,
        levelName = level.name,
        movesUsed = movesUsed,
        moveLimit = level.moveLimit,
        hintsLeft = hintsLeft(),
        selected = selected,
        highlight = highlight,
        linked = board.linked
    )

    override fun onCleared() {
        assistJob?.cancel()
        unlockJob?.cancel()
        endJob?.cancel()
        assistJob = null
        unlockJob = null
        endJob = null
        super.onCleared()
    }
}
