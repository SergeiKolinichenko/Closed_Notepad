package info.sergeikolinichenko.closednotepad.presentation.stateful

import info.sergeikolinichenko.closednotepad.models.Note

/** Created by Sergei Kolinichenko on 06.12.2022 at 12:07 (GMT+3) **/

sealed class StateNoteEdit

class NoteEditNote(val note: Note?): StateNoteEdit()
class ColorIndex(val index: Int): StateNoteEdit()
data object ShowExtraFABs: StateNoteEdit()
data object HideExtraFABs: StateNoteEdit()
data object Lock: StateNoteEdit()
data object Unlock: StateNoteEdit()
data object RetryNoteListFragment: StateNoteEdit()
data object GetNoteListFragment: StateNoteEdit()
data object ShowColorButtonsNoteEdit: StateNoteEdit()
data object HideColorButtonsNoteEdit: StateNoteEdit()
