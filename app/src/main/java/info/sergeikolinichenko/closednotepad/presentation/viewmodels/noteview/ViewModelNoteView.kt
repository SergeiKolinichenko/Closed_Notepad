package info.sergeikolinichenko.closednotepad.presentation.viewmodels.noteview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.RemoveNoteUseCase
import kotlinx.coroutines.launch

class ViewModelNoteView(
    private val getNoteEntryUseCase: GetNoteUseCase,
    private val removeNoteUseCase: RemoveNoteUseCase
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

    fun getNoteEntry(timeStamp: Long) {
        viewModelScope.launch {
            _note.value = getNoteEntryUseCase.invoke(timeStamp)
        }
    }

    fun removeNote() {
        note.value?.let {
            viewModelScope.launch {
                removeNoteUseCase.invoke(it)
            }
        }
        _endUsingFragment.value = Unit
    }

    fun showToast(str: String) {
        _toast.value = str
    }

}