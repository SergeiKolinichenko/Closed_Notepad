package info.sergeikolinichenko.closednotepad.presentation.viewmodels.noteedit

import android.app.backup.BackupManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.presentation.utils.NoteColors
import info.sergeikolinichenko.closednotepad.presentation.utils.NotesBackupAgent
import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepositoryImpl
import info.sergeikolinichenko.closednotepad.usecases.notepad.AddNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.EditNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefColorIndexUseCase
import kotlinx.coroutines.launch

class ViewModelNoteEdit(
    private val getNoteEntryUseCase: GetNoteUseCase,
    private val addNoteEntryUseCase: AddNoteUseCase,
    private val editNoteEntryUseCase: EditNoteUseCase,
    getPrefColorIndex: GetPrefColorIndexUseCase,
    private val backupManager: BackupManager
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

    private var _getSaveOption = MutableLiveData<Boolean>()
    val getSaveOption: LiveData<Boolean>
        get() = _getSaveOption

    private var _isShowColorFabs = MutableLiveData<Boolean>()
    val isShowColorFabs: LiveData<Boolean>
        get() = _isShowColorFabs

    init {
        val colorIndex = getPrefColorIndex.invoke()
        _colorIndex.value =
            if (colorIndex != PreferencesRepositoryImpl.ERROR_GET_INT)
                getPrefColorIndex.invoke()
            else NoteColors.GRAY

    }

    fun getNoteEntry() {
        viewModelScope.launch {
            _note.value = getNoteEntryUseCase.invoke(timeStamp)
        }
    }

    fun addNote(inTitle: String?, inItself: String?) {
        val title = parseString(inTitle)
        val itself = parseString(inItself)

        if (title.isEmpty() && itself.isEmpty()) retryNoteListFragment()
        else _note.value = parseNote(title, itself)
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
            retryNoteListFragment()
        } else {
            _note.value = parseNote(title, itself)
//            if (getSaveOption.value != true) {
//                _getSaveOption.value = true
//            }
        }
    }

    fun addNoteToBase() {
        note.value?.let {
            viewModelScope.launch {
                addNoteEntryUseCase.invoke(it)
            }
        }
        NotesBackupAgent.requestBackup(backupManager)
        retryNoteListFragment()
    }

    fun editNoteToBase() {
        note.value?.let {
            viewModelScope.launch {
                editNoteEntryUseCase.invoke(it)
            }
        }
        NotesBackupAgent.requestBackup(backupManager)
        retryNoteListFragment()
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
        if (title.length > MAX_TITLE_LENGTH) {
            title = makeTitle(title)
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

        var title = if (itself.length > MAX_TITLE_LENGTH) {
            val index = getSubstringIndex(itself)
            itself.substring(0, index)
        } else itself

        val firstChar = title.substring(0, 1)
        if (firstChar != firstChar.uppercase()) {
            title = setFirstCharUppercase(title)
        }

        return title
    }

    private fun getSubstringIndex(string: String): Int {
        // get the number of characters up to the first dot
        var index = string.indexOf(".")
        // if there are more characters or no dot
        if (index < 0 || index > MAX_TITLE_LENGTH) {
            // get the number of MAX_TITLE_LENGTH characters up to the last space
            index = string.lastIndexOf(" ")
            // if there are more MAX_TITLE_LENGTH characters and there are spaces
            if (index > 0 && index > MAX_TITLE_LENGTH) {
                // get the number of characters up to the last space, but less MAX_TITLE_LENGTH
                var lString = string
                do {
                    index = lString.lastIndexOf(" ")
                    lString = lString.substring(0, index)
                } while (index >= MAX_TITLE_LENGTH)
                // if there are no spaces and characters greater than 0
            } else if (index < 0 && string.isNotEmpty()) {
                index = MAX_TITLE_LENGTH
            }
        }
        return index
    }

    private fun setFirstCharUppercase(string: String): String {
        val firstChar = string.substring(0, 1)
        val firstCharUpperCase = firstChar.uppercase()
        val otherString = string.substring(1)
        return firstCharUpperCase + otherString
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
        setShowColorFabs(false)
    }

    fun setShowColorFabs(state: Boolean) {
        _isShowColorFabs.value = state
    }

    fun setSaveOption() {
        _getSaveOption.value = getSaveOption.value == null || getSaveOption.value == false
    }

    fun retryNoteListFragment() {
        _retryNoteListFrag.value = Unit
    }

    companion object {
        private const val MAX_TITLE_LENGTH = 16
    }

}