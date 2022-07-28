package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import androidx.lifecycle.*
import info.sergeikolinichenko.closednotepad.models.NoteEntry
import info.sergeikolinichenko.closednotepad.usecases.notepad.AddEntryToNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.EditEntryAtNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetListNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.RemoveEntryFromNoteUseCase
import java.util.*

class ViewModelNoteList(
    private val getListNoteUseCase: GetListNoteUseCase,
    private val removeEntryFromNoteUseCase: RemoveEntryFromNoteUseCase,
    private val editEntryAtNoteUseCase: EditEntryAtNoteUseCase,
    private val addEntryToNoteUseCase: AddEntryToNoteUseCase
) : ViewModel() {

    private var _noteList = MutableLiveData<List<NoteEntry>>()
    val noteList: LiveData<List<NoteEntry>>
        get() = _noteList


    private val _isSelected = MutableLiveData(false)
    val isSelected: LiveData<Boolean>
        get() = _isSelected

    private val _colorIndex = MutableLiveData<Int>()
    val colorIndex: LiveData<Int>
    get() = _colorIndex

    private lateinit var sortedSet: SortedSet<NoteEntry>
    private val selectedNoteEntries = mutableListOf<NoteEntry>()

    init {
        sortedSet = getSortedTimeStampList()
    }


//    init {
//        _isSelected.value = false
//        var tempCount = 0
//        for (i in 0..50) {
//            if (tempCount == 7) tempCount = 0
//            val note = NoteEntry(
//                timeStamp = Date().time,
//                titleEntry = "This is Title $i",
//                itselfEntry = "This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.This is text entry $i for test. Цикл for в Kotlin имеет другой синтаксис. Применим в тех случаях, когда есть итератор - массив, Map и т.д. Стандартный вариант, когда нужно пробежаться по заданному числу элементов, описывается следующим образом. Если для цикла используется одна команда, можно обойтись без фигурных скобок, но проще всегда использовать блок.",
//                colorIndex = tempCount,
//                isLocked = Random.nextBoolean(),
//                isSelected = false
//            )
//            tempCount++
//            addEntryToNoteUseCase.invoke(note)
//        }
//    }

    fun getNoteList() {
        sortedSet.addAll(getListNoteUseCase.invoke())
        updateNoteList()
    }

    fun selectEntriesAtNote(noteEntry: NoteEntry) {
        if (isSelected.value == false) {
            _isSelected.value = true
        }
        if (selectedNoteEntries.contains(noteEntry)) {
            resetSelectedEntries()
        } else {
            val newItem = noteEntry.copy(isSelected = !noteEntry.isSelected)
            selectedNoteEntries.add(newItem)
            sortedSet.remove(noteEntry)
            sortedSet.add(newItem)
            updateNoteList()
        }
    }

    fun resetSelectedEntries() {
        for (item in selectedNoteEntries) {
            val newItem = item.copy(isSelected = !item.isSelected)
            sortedSet.remove(item)
            sortedSet.add(newItem)
        }
        clearSelectedNoteEntries()
    }

    fun removeEntriesFromNote() {
        for (item in selectedNoteEntries) {
            removeEntryFromNoteUseCase.invoke(item)
            sortedSet.remove(item)
        }
        clearSelectedNoteEntries()
    }

    fun setColorIndex(colorIndex: Int) {
        _colorIndex.value = colorIndex
        for (item in selectedNoteEntries) {
            val newItem = item.copy(colorIndex = colorIndex, isSelected = !item.isSelected)
            editEntryAtNoteUseCase.invoke(newItem)
            sortedSet.remove(item)
            sortedSet.add(newItem)
        }
        clearSelectedNoteEntries()
    }

    private fun clearSelectedNoteEntries() {
        selectedNoteEntries.clear()
        _isSelected.value = false
        updateNoteList()
    }

    private fun getSortedTimeStampList(): SortedSet<NoteEntry> {
        return sortedSetOf<NoteEntry>(
            { i1, i2 -> i1.timeStamp.compareTo(i2.timeStamp) }
        )
    }


    private fun updateNoteList() {
        _noteList.value = sortedSet.toList()
    }
}