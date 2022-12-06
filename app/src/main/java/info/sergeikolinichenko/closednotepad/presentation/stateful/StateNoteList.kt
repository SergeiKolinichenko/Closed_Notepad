package info.sergeikolinichenko.closednotepad.presentation.stateful

/** Created by Sergei Kolinichenko on 03.12.2022 at 21:12 (GMT+3) **/

sealed class StateNoteList

object ItemSelected : StateNoteList()
object ItemUnselected : StateNoteList()
object ShowColorButtonsNoteList : StateNoteList()
object HideColorButtonsNoteList : StateNoteList()
object ShowOrderButtons : StateNoteList()
object HideOrderButtons : StateNoteList()
