package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import info.sergeikolinichenko.closednotepad.models.NoteEntry
import info.sergeikolinichenko.closednotepad.usecases.notepad.*

class ViewModelNoteList(
    private val getListNoteUseCase: GetListNoteUseCase,
    private val removeEntryFromNoteUseCase: RemoveEntryFromNoteUseCase
): ViewModel() {

    private var _noteList = MutableLiveData<List<NoteEntry>>()
    val noteList: LiveData<List<NoteEntry>>
    get() = _noteList

    private val _isSelected = MutableLiveData(false)
    val isSelected : LiveData<Boolean>
        get() = _isSelected

    init {
        _isSelected.value = false
    }

    fun getNoteList() {
        _noteList.value = getListNoteUseCase.invoke()
    }

    fun selectEntriesAtNote(noteEntry: NoteEntry) {
            if (_isSelected.value == false) {
                _isSelected.value = true
            }
        //selectEntryAtNoteUseCase.invoke(noteEntry)
        getNoteList()
    }

    fun resSelEntriesAtNote() {
        //resSelEntriesNoteUseCase.invoke()
        _isSelected.value = false
        getNoteList()
    }

    fun resSelEntriesAtNote(noteEntry: NoteEntry) {
        if (noteEntry.isSelected) {
            //resSelEntriesNoteUseCase.invoke()
            _isSelected.value = false
        } else{
            //selectEntryAtNoteUseCase.invoke(noteEntry)
        }
        getNoteList()
    }

    fun removeEntryFromNote(noteEntry: NoteEntry) {
        removeEntryFromNoteUseCase.invoke(noteEntry)
        getNoteList()
    }

    fun removeEntriesFromNote() {
        //removeEntriesFromNoteUseCase.invoke()
        _isSelected.value = false
        getNoteList()
    }
}