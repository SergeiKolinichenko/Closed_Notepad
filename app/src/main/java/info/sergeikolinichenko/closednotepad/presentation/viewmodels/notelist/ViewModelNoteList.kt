package info.sergeikolinichenko.closednotepad.presentation.viewmodels.notelist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.usecases.notepad.AddNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.EditNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetListNotesUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.RemoveNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefOrderNoteListUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.SetPrefOrderNoteListUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.AddRemovedNoteUseCase
import kotlinx.coroutines.launch

class ViewModelNoteList(
    getListNote: GetListNotesUseCase,
    getPrefOrderListNote: GetPrefOrderNoteListUseCase,
    private val removeNote: RemoveNoteUseCase,
    private val editNote: EditNoteUseCase,
    private val setPrefOrderListNote: SetPrefOrderNoteListUseCase,
    private val addRemovedNote: AddRemovedNoteUseCase,
    private val addEntryToNoteUseCase: AddNoteUseCase
) : ViewModel() {

    private var _noteList: MutableLiveData<List<Note>> = getListNote.invoke()
    val noteList: LiveData<List<Note>>
    get() = _noteList

    private val _isSelected = MutableLiveData(false)
    val isSelected: LiveData<Boolean>
        get() = _isSelected

    private var _orderViewNoteList: String? = getPrefOrderListNote.invoke()
    val orderViewNoteList: String
    get() = _orderViewNoteList ?: throw RuntimeException("orderViewNoteList equal null")

    private val selectedNotes = mutableListOf<Note>()


//    init {
//        _isSelected.value = false
//        var tempCount = 0
//        for (i in 0..100) {
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

    fun selectNotesAtNote(noteEntry: Note) {
        val notes: MutableList<Note> = noteList.value?.toMutableList() ?:
        throw RuntimeException("noteList equals null")

        if (isSelected.value == false) {
            _isSelected.value = true
        }

        if (selectedNotes.contains(noteEntry)) {
            resetSelectedNotes()
        } else {
            val newItem = noteEntry.copy(isSelected = !noteEntry.isSelected)
            selectedNotes.add(newItem)
            notes.remove(noteEntry)
            notes.add(newItem)
            _noteList.value = notes
        }
    }

    fun resetSelectedNotes() {
        val notes: MutableList<Note> = noteList.value?.toMutableList() ?:
        throw RuntimeException("noteList equals null")

        for (item in selectedNotes) {
            val newItem = item.copy(isSelected = !item.isSelected)
            notes.remove(item)
            notes.add(newItem)
            _noteList.value = notes
        }
        clearSelectedNotes()
    }

    fun removeNotes() {
        for (item in selectedNotes) {
            viewModelScope.launch {
                addRemovedNote.invoke(removeNote.invoke(item))
            }
        }
        clearSelectedNotes()
    }

    fun setColorIndex(colorIndex: Int) {
        for (item in selectedNotes) {
            val newItem = item.copy(colorIndex = colorIndex, isSelected = !item.isSelected)
            viewModelScope.launch {
                editNote.invoke(newItem)
            }
        }
        clearSelectedNotes()
    }

    private fun clearSelectedNotes() {
        selectedNotes.clear()
        _isSelected.value = false
    }

    fun setOrderViewNoteList(order: String) {
        _orderViewNoteList = order
        setPrefOrderListNote.invoke(order)
        _noteList.value = noteList.value
    }

}