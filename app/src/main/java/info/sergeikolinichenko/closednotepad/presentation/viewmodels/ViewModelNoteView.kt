package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.RemoveNoteUseCase
import kotlinx.coroutines.launch

class ViewModelNoteView(
    private val getNoteEntryUseCase: GetNoteUseCase
) : ViewModel() {

    private var _note = MutableLiveData<Note>()
    val note: LiveData<Note>
        get() = _note

    var noteToNoteListFrag = MutableLiveData<Note>()

    private var _endUsingFragment = MutableLiveData<Unit>()
    val endUsingFragment: LiveData<Unit>
        get() = _endUsingFragment

    private var _buttonDeleteNote = MutableLiveData<Unit>()
    val buttonDeleteNote: LiveData<Unit>
        get() = _buttonDeleteNote


    private var _buttonSendNote = MutableLiveData<Unit>()
    val buttonSendNote: LiveData<Unit>
        get() = _buttonSendNote

    private var _buttonCopyContent = MutableLiveData<Unit>()
    val buttonCopyContent: LiveData<Unit>
        get() = _buttonCopyContent

    var buttonEditNote = MutableLiveData<Unit>()

    private var _toast = MutableLiveData<String>()
    val toast: LiveData<String>
        get() = _toast

    fun getNoteEntry(timeStamp: Long) {
        viewModelScope.launch {
            _note.value = getNoteEntryUseCase.invoke(timeStamp)
        }
    }

    fun removeNote() {
        noteToNoteListFrag.value = note.value
    }

    fun endUsingFragment() {
        _endUsingFragment.value = Unit
    }

    fun pushButtonCopyContent() {
        _buttonCopyContent.value = Unit
    }

    fun pushButtonSendNote() {
        _buttonSendNote.value = Unit
    }

    fun pushButtonDelete() {
        _buttonDeleteNote.value = Unit
    }

    fun showToast(str: String) {
        _toast.value = str
    }

}