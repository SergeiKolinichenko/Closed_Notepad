package info.sergeikolinichenko.closednotepad.presentation.stateful

/** Created by Sergei Kolinichenko on 06.12.2022 at 20:00 (GMT+3) **/

sealed class StateNotesMenu

class DefaultColorIndex(val index: Int): StateNotesMenu()
class DaysBeforeDelete(val days: Int): StateNotesMenu()

object ShowColorButtonsNotesMenu: StateNotesMenu()
object HideColorButtonsNotesMenu: StateNotesMenu()

object ShowSetDaysButtons: StateNotesMenu()
object HideSetDaysButtons: StateNotesMenu()
