package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.closednotepad.models.Note
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

    private var _note = MutableLiveData<Note>()
    val note: LiveData<Note>
        get() = _note

    private var _endUsingFragment = MutableLiveData<Unit>()
    val endUsingFragment: LiveData<Unit>
        get() = _endUsingFragment

    private var _toast = MutableLiveData<String>()
    val toast: LiveData<String>
        get() = _toast

    fun getNote(timeStamp: Long) {
        viewModelScope.launch {
            _note.value = getNoteEntryUseCase.invoke(timeStamp)
        }
    }

    fun removeNote() {
        note.value?.let {
            viewModelScope.launch {
                addRemovedNote.invoke(removeNoteUseCase.invoke(it))
            }
        }
        _endUsingFragment.value = Unit
    }

}