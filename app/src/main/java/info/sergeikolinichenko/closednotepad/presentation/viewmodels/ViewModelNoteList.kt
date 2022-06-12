package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import info.sergeikolinichenko.closednotepad.models.NoteEntry
import info.sergeikolinichenko.closednotepad.repository.NoteRepositoryImpl
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetListNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.RemoveEntryFromNoteUseCase

class ViewModelNoteList: ViewModel() {

    private val repository = NoteRepositoryImpl
    val noteList = MutableLiveData<List<NoteEntry>>()

    private val getListNoteUseCase = GetListNoteUseCase(repository)
    private val removeEntryFromNoteUseCase = RemoveEntryFromNoteUseCase(repository)

    fun getNoteList() {
        noteList.value = getListNoteUseCase.invoke()
    }
    fun removeEntryFromNote(noteEntry: NoteEntry) {
        removeEntryFromNoteUseCase.invoke(noteEntry)
        getNoteList()
    }
}