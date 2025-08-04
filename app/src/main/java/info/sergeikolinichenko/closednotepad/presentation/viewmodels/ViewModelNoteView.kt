package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.closednotepad.presentation.stateful.EndUsing
import info.sergeikolinichenko.closednotepad.presentation.stateful.NoteViewNote
import info.sergeikolinichenko.closednotepad.presentation.stateful.StateNoteView
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.RemoveNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.AddRemovedNoteUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

class ViewModelNoteView @Inject constructor(
    private val getNoteEntryUseCase: GetNoteUseCase,
    private val removeNoteUseCase: RemoveNoteUseCase,
    private val addRemovedNote: AddRemovedNoteUseCase
) : ViewModel() {

    private var _stateNoteView = MutableLiveData<StateNoteView>()
    val stateNoteView: LiveData<StateNoteView>
        get() = _stateNoteView

    fun getNote(timeStamp: Long) {
        viewModelScope.launch {
            _stateNoteView.value = NoteViewNote(note = getNoteEntryUseCase.invoke(timeStamp))
        }
    }

    fun removeNote(timeStamp: Long) {
        viewModelScope.launch {
            val note = getNoteEntryUseCase.invoke(timeStamp)
            addRemovedNote.invoke(removeNoteUseCase.invoke(note))
        }

        _stateNoteView.value = EndUsing
    }

}