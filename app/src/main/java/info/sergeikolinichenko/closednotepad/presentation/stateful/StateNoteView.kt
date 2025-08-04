package info.sergeikolinichenko.closednotepad.presentation.stateful

import info.sergeikolinichenko.closednotepad.models.Note

/** Created by Sergei Kolinichenko on 05.12.2022 at 21:48 (GMT+3) **/

sealed class StateNoteView

data object EndUsing: StateNoteView()
class NoteViewNote(val note: Note): StateNoteView()
