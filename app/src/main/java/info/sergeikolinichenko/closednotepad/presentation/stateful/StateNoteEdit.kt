package info.sergeikolinichenko.closednotepad.presentation.stateful

import info.sergeikolinichenko.closednotepad.models.Note

/** Created by Sergei Kolinichenko on 06.12.2022 at 12:07 (GMT+3) **/

sealed class StateNoteEdit

class NoteEditNote(val note: Note?): StateNoteEdit()
class ColorIndex(val index: Int): StateNoteEdit()
object ShowExtraFABs: StateNoteEdit()
object HideExtraFABs: StateNoteEdit()
object Lock: StateNoteEdit()
object Unlock: StateNoteEdit()
object RetryNoteListFragment: StateNoteEdit()
object ShowColorButtonsNoteEdit: StateNoteEdit()
object HideColorButtonsNoteEdit: StateNoteEdit()
