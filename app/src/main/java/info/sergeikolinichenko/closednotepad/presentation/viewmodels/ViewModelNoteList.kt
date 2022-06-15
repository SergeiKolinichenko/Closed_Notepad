package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import info.sergeikolinichenko.closednotepad.models.NoteEntry
import info.sergeikolinichenko.closednotepad.repository.NoteRepositoryImpl
import info.sergeikolinichenko.closednotepad.usecases.notepad.*

class ViewModelNoteList: ViewModel() {

    private val repository = NoteRepositoryImpl
    val noteList = MutableLiveData<List<NoteEntry>>()

    private val _isSelected = MutableLiveData(false)
    val isSelected : LiveData<Boolean>
        get() = _isSelected

    init {
        _isSelected.value = false
    }

    private val getListNoteUseCase = GetListNoteUseCase(repository)
    private val removeEntryFromNoteUseCase = RemoveEntryFromNoteUseCase(repository)
    private val removeEntriesFromNoteUseCase = RemoveEntriesFromNoteUseCase(repository)
    private val selectEntryAtNoteUseCase = SelectEntryAtNoteUseCase(repository)
    private val resSelEntriesNoteUseCase = ResSelEntriesNoteUseCase(repository)


    fun getNoteList() {
        noteList.value = getListNoteUseCase.invoke()
    }

    fun selectEntriesAtNote(noteEntry: NoteEntry) {
            if (_isSelected.value == false) {
                _isSelected.value = true
            }
        selectEntryAtNoteUseCase.invoke(noteEntry)
        getNoteList()
    }

    fun resSelEntriesAtNote(noteEntry: NoteEntry) {
        if (noteEntry.isSelected) {
            resSelEntriesNoteUseCase.invoke()
            _isSelected.value = false
        } else{
            selectEntryAtNoteUseCase.invoke(noteEntry)
        }
        getNoteList()
    }

    fun removeEntryFromNote(noteEntry: NoteEntry) {
        removeEntryFromNoteUseCase.invoke(noteEntry)
        getNoteList()
    }

    fun removeEntriesFromNote() {
        removeEntriesFromNoteUseCase.invoke()
        _isSelected.value = false
        getNoteList()
    }
}