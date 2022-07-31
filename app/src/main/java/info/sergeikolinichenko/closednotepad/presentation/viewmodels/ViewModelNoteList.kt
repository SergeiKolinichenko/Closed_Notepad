package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.usecases.notepad.AddNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.EditNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetListNotesUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.RemoveNoteUseCase
import kotlinx.coroutines.launch
import java.util.*

class ViewModelNoteList(
    private val getListNoteUseCase: GetListNotesUseCase,
    private val removeNoteUseCase: RemoveNoteUseCase,
    private val editNoteUseCase: EditNoteUseCase,
    private val addEntryToNoteUseCase: AddNoteUseCase
) : ViewModel() {

    private var _noteList = MutableLiveData<List<Note>>()
    val noteList: LiveData<List<Note>>
        get() = _noteList


    private val _isSelected = MutableLiveData(false)
    val isSelected: LiveData<Boolean>
        get() = _isSelected

    private val _colorIndex = MutableLiveData<Int>()
    val colorIndex: LiveData<Int>
    get() = _colorIndex

    private lateinit var sortedSet: SortedSet<Note>
    private val selectedNotes = mutableListOf<Note>()

    init {
        sortedSet = getSortedTimeStampList()
    }

//    init {
//        _isSelected.value = false
//        var tempCount = 0
//        for (i in 0..50) {
//            if (tempCount == 7) tempCount = 0
//            val note = Note(
//                timeStamp = Date().time,
//                titleNote = "This is Title $i",
//                itselfNote = "This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.",
//                colorIndex = tempCount,
//                isLocked = Random.nextBoolean(),
//                isSelected = false
//            )
//            tempCount++
//            viewModelScope.launch {
//                addEntryToNoteUseCase.invoke(note)
//            }
//        }
//    }

    fun getNoteList() {
        viewModelScope.launch {
            sortedSet.addAll(getListNoteUseCase.invoke())
            updateNoteList()
        }
    }

    fun selectEntriesAtNote(noteEntry: Note) {
        if (isSelected.value == false) {
            _isSelected.value = true
        }
        if (selectedNotes.contains(noteEntry)) {
            resetSelectedNotes()
        } else {
            val newItem = noteEntry.copy(isSelected = !noteEntry.isSelected)
            selectedNotes.add(newItem)
            sortedSet.remove(noteEntry)
            sortedSet.add(newItem)
            updateNoteList()
        }
    }

    fun resetSelectedNotes() {
        for (item in selectedNotes) {
            val newItem = item.copy(isSelected = !item.isSelected)
            sortedSet.remove(item)
            sortedSet.add(newItem)
        }
        clearSelectedNotes()
    }

    fun removeNote(timeStamp: Long) {
        val note = sortedSet.find { it.timeStamp == timeStamp }
        note?.let {
            viewModelScope.launch {
                removeNoteUseCase.invoke(it)
                sortedSet.remove(it)
                updateNoteList()
            }
        }
    }

    fun removeNotes() {
        for (item in selectedNotes) {
            viewModelScope.launch {
                removeNoteUseCase.invoke(item)
            }
            sortedSet.remove(item)
        }
        clearSelectedNotes()
    }

    fun setColorIndex(colorIndex: Int) {
        _colorIndex.value = colorIndex
        for (item in selectedNotes) {
            val newItem = item.copy(colorIndex = colorIndex, isSelected = !item.isSelected)
            viewModelScope.launch {
                editNoteUseCase.invoke(newItem)
            }
            sortedSet.remove(item)
            sortedSet.add(newItem)
        }
        clearSelectedNotes()
    }

    private fun clearSelectedNotes() {
        selectedNotes.clear()
        _isSelected.value = false
        updateNoteList()
    }

    private fun getSortedTimeStampList(): SortedSet<Note> {
        return sortedSetOf<Note>(
            { i1, i2 -> i1.timeStamp.compareTo(i2.timeStamp) }
        )
    }


    private fun updateNoteList() {
        _noteList.value = sortedSet.toList()
    }
}