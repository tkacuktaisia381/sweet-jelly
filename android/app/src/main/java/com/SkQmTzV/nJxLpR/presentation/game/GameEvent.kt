package com.SkQmTzV.nJxLpR.presentation.game

import com.SkQmTzV.nJxLpR.domain.model.Cell
import com.SkQmTzV.nJxLpR.domain.model.Swap

sealed class GameEvent {

    data class Swapped(val swap: Swap) : GameEvent()

    data class Rejected(val cell: Cell) : GameEvent()

    data class Hinted(val cells: Set<Cell>) : GameEvent()

    data class Linked(val cells: List<Cell>) : GameEvent()

    object Restarted : GameEvent()
}
