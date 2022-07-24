package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import info.sergeikolinichenko.closednotepad.models.NoteEntry
import info.sergeikolinichenko.closednotepad.presentation.utils.EntriesColors
import info.sergeikolinichenko.closednotepad.usecases.notepad.AddEntryToNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetListNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.RemoveEntryFromNoteUseCase
import java.util.*
import kotlin.random.Random

class ViewModelNoteList(
    private val getListNoteUseCase: GetListNoteUseCase,
    private val removeEntryFromNoteUseCase: RemoveEntryFromNoteUseCase,
    private val addEntryToNoteUseCase: AddEntryToNoteUseCase
): ViewModel() {

    private var _noteList = MutableLiveData<List<NoteEntry>>()
    val noteList: LiveData<List<NoteEntry>>
    get() = _noteList

    private val _isSelected = MutableLiveData(false)
    val isSelected : LiveData<Boolean>
        get() = _isSelected

//    init {
//        _isSelected.value = false
//        var tempCount = 0
//        for (i in 0..50) {
//            if (tempCount == 7) tempCount = 0
//            val note = NoteEntry(
//                timeStamp = Date().time,
//                titleEntry = "This is Title $i",
//                itselfEntry = "This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.",
//                colorIndex = tempCount,
//                isLocked = Random.nextBoolean(),
//                isSelected = false
//            )
//            tempCount++
//            addEntryToNoteUseCase.invoke(note)
//        }
//    }

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