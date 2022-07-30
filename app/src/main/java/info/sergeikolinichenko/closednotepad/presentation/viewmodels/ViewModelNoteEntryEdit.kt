package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.usecases.notepad.EditNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetNoteUseCase
import kotlinx.coroutines.launch

class ViewModelNoteEntryEdit(
    private val getNoteEntryUseCase: GetNoteUseCase,
    private val editNoteEntryUseCase: EditNoteUseCase
): ViewModel() {

    private var _noteEntry = MutableLiveData<Note>()
    val noteEntry: LiveData<Note>
        get() = _noteEntry

    fun getNoteEntry(timeStamp: Long) {
        viewModelScope.launch {
            _noteEntry.value = getNoteEntryUseCase.invoke(timeStamp)
        }
    }
}