package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import info.sergeikolinichenko.closednotepad.models.NoteEntry
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetNoteEntryUseCase

class ViewModelNoteEntryView(
    private val getNoteEntryUseCase: GetNoteEntryUseCase
): ViewModel() {

    private var _noteEntry = MutableLiveData<NoteEntry>()
    val noteEntry: LiveData<NoteEntry>
    get() = _noteEntry

    fun getNoteEntry(timeStamp: Long) {
        _noteEntry.value = getNoteEntryUseCase.invoke(timeStamp)
    }

}