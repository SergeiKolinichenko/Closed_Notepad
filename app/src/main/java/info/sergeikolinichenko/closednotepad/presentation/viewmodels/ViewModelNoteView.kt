package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetNoteUseCase
import kotlinx.coroutines.launch

class ViewModelNoteView(
    private val getNoteEntryUseCase: GetNoteUseCase
): ViewModel() {

    private var _noteEntry = MutableLiveData<Note>()
    val noteEntry: LiveData<Note>
    get() = _noteEntry

    var buttonDelete = MutableLiveData<Unit>()
    var buttonSend = MutableLiveData<Unit>()
    var buttonCopyContent = MutableLiveData<Unit>()
    var buttonEdii = MutableLiveData<Unit>()

    fun getNoteEntry(timeStamp: Long) {
        viewModelScope.launch {
            _noteEntry.value = getNoteEntryUseCase.invoke(timeStamp)
        }
    }

}