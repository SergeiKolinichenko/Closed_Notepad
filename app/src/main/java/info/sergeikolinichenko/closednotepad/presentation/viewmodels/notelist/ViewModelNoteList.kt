package info.sergeikolinichenko.closednotepad.presentation.viewmodels.notelist

import android.app.backup.BackupManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.RemovedNote
import info.sergeikolinichenko.closednotepad.presentation.utils.TimeUtils
import info.sergeikolinichenko.closednotepad.usecases.notepad.AddNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.EditNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetListNotesUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.RemoveNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefAutoDelReNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefOrderNoteListUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.SetPrefOrderNoteListUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.AddRemovedNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.DeleteRemovedNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.trashcan.GetListRemovedNoteUseCase
import info.sergeikolinichenko.closednotepad.utils.NotesBackupAgent
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random
import kotlin.random.Random.Default.nextBoolean

class ViewModelNoteList(
    getListNote: GetListNotesUseCase,
    private val removeNote: RemoveNoteUseCase,
    private val editNote: EditNoteUseCase,
    private val getRemovedNoteList: GetListRemovedNoteUseCase,
    private val addRemovedNote: AddRemovedNoteUseCase,
    private val deleteRemovedNote: DeleteRemovedNoteUseCase,
    getPrefOrderListNote: GetPrefOrderNoteListUseCase,
    private val setPrefOrderListNote: SetPrefOrderNoteListUseCase,
    private val getPrefDayBeforeDelete: GetPrefAutoDelReNoteUseCase,
    private val backupManager: BackupManager,
    private val addEntryToNoteUseCase: AddNoteUseCase
) : ViewModel() {

    private var _noteList: MutableLiveData<List<Note>> = getListNote.invoke()
    val noteList: LiveData<List<Note>>
        get() = _noteList

    private var _orderViewNoteList: String? = getPrefOrderListNote.invoke()
    val orderViewNoteList: String
        get() = _orderViewNoteList ?: throw RuntimeException("orderViewNoteList equal null")

    private val _isSelected = MutableLiveData(false)
    val isSelected: LiveData<Boolean>
        get() = _isSelected

    private var _showColorButtons = MutableLiveData<Boolean>()
    val showColorButtons: LiveData<Boolean>
        get() = _showColorButtons

    private var _showOrderButtons = MutableLiveData<Boolean>()
    val showOrderButtons: LiveData<Boolean>
        get() = _showOrderButtons

    private val selectedNotes = mutableListOf<Note>()

    init {
        autoDeleteRemovedNote()
    }


//    init {
//        _isSelected.value = false
//        var tempCount = 0
//        for (i in 0..200) {
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

    fun selectNotes(note: Note) {
        if (isSelected.value == false || isSelected.value == null) _isSelected.value = true

        if (selectedNotes.contains(note)) {
            resetSelectedNotes()
        } else {
            val notes: MutableList<Note> =
                noteList.value?.toMutableList() ?: throw RuntimeException("noteList equals null")

            val newItem = note.copy(isSelected = !note.isSelected)
            selectedNotes.add(newItem)
            notes.remove(note)
            notes.add(newItem)
            _noteList.value = notes
        }
    }

    fun resetSelectedNotes() {
        val notes: MutableList<Note> =
            noteList.value?.toMutableList() ?: throw RuntimeException("noteList equals null")

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
        backupManager.dataChanged()
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
        backupManager.dataChanged()
        _showColorButtons.value = false
    }

    private fun clearSelectedNotes() {
        selectedNotes.clear()
        _isSelected.value = false
    }

    fun setOrderViewNoteList(order: String) {
        _orderViewNoteList = order
        setPrefOrderListNote.invoke(order)
        _noteList.value = noteList.value
        _showOrderButtons.value = false
        backupManager.dataChanged()
    }

    fun setStateShowColorButtons() {
        _showColorButtons.value =
            showColorButtons.value == null || showColorButtons.value == false
    }

    fun setStateShowOrderButtons() {
        _showOrderButtons.value =
            showOrderButtons.value == null || showOrderButtons.value == false
    }

    private fun autoDeleteRemovedNote() {
        val days = getPrefDayBeforeDelete.invoke()
        if (days > 0) {
            val list = getRemovedNoteList.invoke<LiveData<List<RemovedNote>>>().value
            list?.let {
                for (item in list) {
                    if (TimeUtils.getDiffDays(item.timeStamp) > days) {
                        viewModelScope.launch {
                            deleteRemovedNote.invoke(item.timeStamp)
                        }
                    }
                }
                backupManager.dataChanged()
            }
        }
    }

}