package info.sergeikolinichenko.closednotepad.presentation.viewmodels.notesearch

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetListNotesUseCase

class ViewModelNoteSearch( getNoteList: GetListNotesUseCase ): ViewModel() {

    val noteList: LiveData<List<Note>> = getNoteList.invoke()

}