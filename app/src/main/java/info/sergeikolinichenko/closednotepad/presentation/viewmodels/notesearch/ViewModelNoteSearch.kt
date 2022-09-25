package info.sergeikolinichenko.closednotepad.presentation.viewmodels.notesearch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetListNotesUseCase

class ViewModelNoteSearch(getNoteList: GetListNotesUseCase) : ViewModel() {

    private val noteList: LiveData<List<Note>> = getNoteList.invoke()
    private val list = mutableListOf<Note>()

    private var _locNoteList = MutableLiveData<List<Note>>()
    val locNoteList: LiveData<List<Note>>
        get() = _locNoteList

    init {
        noteList.observeForever {
            list.addAll(it)
        }
    }

    fun searchNote(search: String) {
        val searchList = mutableSetOf<Note>()
        for (item in list) {
            if (item.titleNote.contains(search, true)) {
                searchList.add(item)
            }
        }
        for (item in list) {
            if (item.itselfNote.contains(search, true)) {
                searchList.add(item)
            }
        }
        _locNoteList.value = searchList.toList()
    }

    override fun onCleared() {
        super.onCleared()
        noteList.removeObserver { list }
    }
}