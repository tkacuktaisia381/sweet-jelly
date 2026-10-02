package com.SkQmTzV.nJxLpR.presentation.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.SkQmTzV.nJxLpR.R
import com.SkQmTzV.nJxLpR.core.di.ServiceLocator
import com.SkQmTzV.nJxLpR.core.di.ViewModelFactory
import com.SkQmTzV.nJxLpR.core.navigation.Navigator
import com.SkQmTzV.nJxLpR.databinding.FragmentGameBinding
import com.SkQmTzV.nJxLpR.domain.model.JellyColor
import com.SkQmTzV.nJxLpR.domain.model.RoundResult
import kotlinx.coroutines.launch

class GameFragment : Fragment() {

    private var binding: FragmentGameBinding? = null
    private var navigated = false

    private val levelId: Int
        get() = arguments?.getInt(ARG_LEVEL_ID) ?: 1

    private val viewModel: GameViewModel by viewModels {
        val context = requireContext().applicationContext
        ViewModelFactory {
            GameViewModel(
                levelId = levelId,
                levelRepository = ServiceLocator.levelRepository(context),
                applySwap = ServiceLocator.applySwap(),
                evaluate = ServiceLocator.evaluateConnections(),
                scoreRound = ServiceLocator.scoreRound(),
                saveProgress = ServiceLocator.saveProgress(context),
                buildBoard = ServiceLocator.buildBoard()
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentGameBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return

        bound.meterCompact.setCompact(true)
        bound.meterFull.setCompact(false)

        bound.board.onTileTap = { cell -> viewModel.onTileTap(cell) }
        bound.btnBack.setOnClickListener { Navigator.back(parentFragmentManager) }
        bound.btnHint.setOnClickListener { viewModel.onHint() }
        bound.btnRestart.setOnClickListener { viewModel.onRestart() }

        observeState()
        observeEvents()
        observeResult()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state -> render(state) }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event -> play(event) }
            }
        }
    }

    private fun observeResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.result.collect { outcome ->
                    if (outcome != null) openResult(outcome)
                }
            }
        }
    }

    private fun render(state: GameUiState) {
        val bound = binding ?: return
        bound.txtLevel.text =
            getString(R.string.game_level_title, state.levelId, state.levelName)
        bound.meterCompact.bind(state.movesLeft, state.moveLimit)
        bound.meterFull.bind(state.movesLeft, state.moveLimit)

        bound.chipPink.bind(JellyColor.PINK, state.linked.contains(JellyColor.PINK))
        bound.chipLemon.bind(JellyColor.LEMON, state.linked.contains(JellyColor.LEMON))
        bound.chipMint.bind(JellyColor.MINT, state.linked.contains(JellyColor.MINT))
        bound.chipBerry.bind(JellyColor.BERRY, state.linked.contains(JellyColor.BERRY))

        bound.board.setInteractive(state.status == GameStatus.PLAYING)
        bound.board.setHighlight(state.highlight)
        bound.board.setSelected(state.selected)
        bound.board.bind(state.board)
        bound.board.alpha = if (state.status == GameStatus.LOST) 0.55f else 1f

        val hintEnabled = state.hintsLeft > 0 && state.status == GameStatus.PLAYING
        bound.btnHint.text = getString(R.string.game_btn_hint, state.hintsLeft)
        bound.btnHint.isEnabled = hintEnabled
        bound.btnHint.alpha = if (hintEnabled) 1f else 0.45f
        bound.btnRestart.isEnabled = state.status == GameStatus.PLAYING
        bound.btnRestart.alpha = if (state.status == GameStatus.PLAYING) 1f else 0.45f
    }

    private fun play(event: GameEvent) {
        val bound = binding ?: return
        when (event) {
            is GameEvent.Swapped -> bound.board.animateSwapIn(event.swap.a, event.swap.b)
            is GameEvent.Rejected -> bound.board.animateReject(event.cell)
            is GameEvent.Hinted -> bound.board.animatePulse(event.cells)
            is GameEvent.Linked -> bound.board.animateLinkWave(event.cells)
            GameEvent.Restarted -> bound.board.cancelAnimations()
        }
    }

    private fun openResult(outcome: RoundResult) {
        try {
            if (navigated || !isAdded || binding == null) return
            navigated = true
            binding?.board?.cancelAnimations()
            Navigator.showResult(parentFragmentManager, outcome)
        } catch (e: Exception) {
            navigated = false
        }
    }

    override fun onDestroyView() {
        binding?.board?.cancelAnimations()
        binding?.board?.onTileTap = null
        binding?.meterCompact?.cancelAnimations()
        binding?.meterFull?.cancelAnimations()
        binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_LEVEL_ID = "arg_level_id"

        fun newInstance(levelId: Int): GameFragment {
            val fragment = GameFragment()
            val args = Bundle()
            args.putInt(ARG_LEVEL_ID, levelId)
            fragment.arguments = args
            return fragment
        }
    }
}
