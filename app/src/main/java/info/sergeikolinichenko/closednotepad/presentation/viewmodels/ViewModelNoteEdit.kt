package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.usecases.notepad.AddNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.EditNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetNoteUseCase
import kotlinx.coroutines.launch

class ViewModelNoteEdit(
    private val getNoteEntryUseCase: GetNoteUseCase,
    private val addNoteEntryUseCase: AddNoteUseCase,
    private val editNoteEntryUseCase: EditNoteUseCase
) : ViewModel() {

    private var _note = MutableLiveData<Note>()
    val note: LiveData<Note>
        get() = _note

    private var _timeStamp: Long? = null
    val timeStamp: Long
        get() = _timeStamp ?: throw RuntimeException("timeStamp equals null")

    private var _isLock = MutableLiveData<Boolean>()
    val isLock: LiveData<Boolean>
        get() = _isLock

    private var _colorIndex = MutableLiveData<Int>()
    val colorIndex: LiveData<Int>
        get() = _colorIndex

    private var _retryNoteListFrag = MutableLiveData<Unit>()
    val retryNoteListFrag: LiveData<Unit>
        get() = _retryNoteListFrag

    private var _getSaveOption = MutableLiveData<Unit>()
    val getSaveOption: LiveData<Unit>
        get() = _getSaveOption

    fun getNoteEntry() {
        viewModelScope.launch {
            _note.value = getNoteEntryUseCase.invoke(timeStamp)
        }
    }

    fun addNote(inTitle: String?, inItself: String?) {
        val title = parseString(inTitle)
        val itself = parseString(inItself)

        if (title.isEmpty() && itself.isEmpty()) {
            retryNoteListFrag()
        } else {
            _note.value = parseNote(title, itself)
            _getSaveOption.value = Unit
        }
    }

    fun editNote(inTitle: String?, inItself: String?) {
        val title = parseString(inTitle)
        val itself = parseString(inItself)

        if (
            inTitle == note.value?.titleNote
            && inItself == note.value?.itselfNote
            && isLock.value == note.value?.isLocked
            && colorIndex.value == note.value?.colorIndex
            || (title.isEmpty() && itself.isEmpty())
        ) {
            retryNoteListFrag()
        } else {
            _note.value = parseNote(title, itself)
            _getSaveOption.value = Unit
        }
    }

    fun addNoteToBase() {
        note.value?.let {
            viewModelScope.launch {
                addNoteEntryUseCase.invoke(it)
            }
        }
        retryNoteListFrag()
    }

    fun editNoteToBase() {
        note.value?.let {
            viewModelScope.launch {
                editNoteEntryUseCase.invoke(it)
            }
        }
        retryNoteListFrag()
    }

    private fun parseNote(inTitle: String, inItself: String): Note {
        var title = inTitle
        var itself = inItself
        val lock: Boolean = isLock.value
            ?: throw RuntimeException("isLock equals null")
        val index: Int = colorIndex.value
            ?: throw RuntimeException("colorIndex equals null")

        if (title.isEmpty()) {
            title = makeTitle(itself)
        } else if (itself.isEmpty()) {
            itself = title
        }
        return Note(
            timeStamp = timeStamp,
            titleNote = title,
            itselfNote = itself,
            colorIndex = index,
            isLocked = lock,
            isSelected = false
        )
    }

    private fun makeTitle(itself: String): String {
        var lItself = itself
        if (itself.length > MAX_TITLE_LENGTH) {
            val index = getSubstringIndex(itself)
            lItself = itself.substring(0, index)
        }
        return lItself
    }

    private fun getSubstringIndex(string: String): Int {
        var lString = string
        var index = string.indexOf(".")
        if (index < 0 || index > MAX_TITLE_LENGTH) {
            index = lString.lastIndexOf(" ")
            if (index > MAX_TITLE_LENGTH) {
                do {
                    index = lString.lastIndexOf(" ")
                    lString.substring(0, index)
                } while (index >= MAX_TITLE_LENGTH)
            } else {
                index = MAX_TITLE_LENGTH
            }
        }
        return index
    }

    private fun parseString(txt: String?) = txt?.trim() ?: ""

    fun setTimeStamp(ts: Long) {
        _timeStamp = ts
    }

    fun setIsLock(lock: Boolean) {
        _isLock.value = lock
    }

    fun changeIsLock() {
        isLock.value?.let {
            _isLock.value = !it
        }
    }

    fun setColorIndex(index: Int) {
        _colorIndex.value = index
    }

    fun retryNoteListFrag() {
        _retryNoteListFrag.value = Unit
    }

    companion object {
        private const val MAX_TITLE_LENGTH = 50
    }

}