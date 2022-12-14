package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import android.app.backup.BackupManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.presentation.stateful.*
import info.sergeikolinichenko.closednotepad.presentation.utils.NoteColors
import info.sergeikolinichenko.closednotepad.presentation.utils.NotesBackupAgent
import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepositoryImpl.Companion.ERROR_GET_INT
import info.sergeikolinichenko.closednotepad.usecases.notepad.AddNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.EditNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.notepad.GetNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefColorIndexUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Created by Sergei Kolinichenko on 28.07.2022 at 18:45 (GMT+3) **/

class ViewModelNoteEdit @Inject constructor(
    private val getNoteEntryUseCase: GetNoteUseCase,
    private val addNoteEntryUseCase: AddNoteUseCase,
    private val editNoteEntryUseCase: EditNoteUseCase,
    private val getPrefColorIndex: GetPrefColorIndexUseCase,
    private val backupManager: BackupManager
) : ViewModel() {

    private var _stateNoteEdit = MutableLiveData<StateNoteEdit>()
    val stateNoteEdit: LiveData<StateNoteEdit>
        get() = _stateNoteEdit

    private var _timeStamp: Long? = null
    val timeStamp: Long
        get() = _timeStamp ?: throw RuntimeException("timeStamp equals null")

    private var note: Note? = null
    private var colorIndex: Int = NoteColors.GRAY
    private var stateShowExtraButton: String = HIDE_EXTRA_FAB
    private var haveParsedNote: String = NOT_HAVE_PARSED_NOTE
    private var noteTitle: String = EMPTY_STRING
    private var noteItself: String = EMPTY_STRING

    fun getAddNoteMode() {
        _stateNoteEdit.value = NoteEditNote(
            Note(
                timeStamp = timeStamp,
                titleNote = EMPTY_STRING,
                itselfNote = EMPTY_STRING,
                colorIndex = colorIndex,
                isLocked = NOTE_UNLOCK,
                isSelected = NOTE_UNSELECTED
            )
        )
    }

    fun getExtraAddNoteMode() {
        if (noteItself.isNotEmpty()) {
            _stateNoteEdit.value = NoteEditNote(
                Note(
                    timeStamp = timeStamp,
                    titleNote = noteTitle,
                    itselfNote = noteItself,
                    colorIndex = colorIndex,
                    isLocked = NOTE_UNLOCK,
                    isSelected = NOTE_UNSELECTED
                )
            )
        } else {
            _stateNoteEdit.value = RetryNoteListFragment
        }
    }

    fun getEditNoteMode() {
        viewModelScope.launch {
            note = getNoteEntryUseCase.invoke(timeStamp)
            _stateNoteEdit.value = NoteEditNote(note = note)
        }
    }

    fun getDefaultColorIndex() {
        colorIndex = if (getPrefColorIndex.invoke() == ERROR_GET_INT) NoteColors.GRAY
        else getPrefColorIndex.invoke()

        _stateNoteEdit.value = ColorIndex(index = colorIndex)

    }

    fun addNote(inTitle: String?, inItself: String?, isLock: Boolean) {
        val title = parseString(inTitle)
        val itself = parseString(inItself)

        if (title.isEmpty()
            && itself.isEmpty()
            && haveParsedNote == NOT_HAVE_PARSED_NOTE
        )
            retryNoteListFragment()
        else note = parseNote(title, itself, isLock)
        _stateNoteEdit.value = NoteEditNote(note = note)
    }

    fun editNote(inTitle: String?, inItself: String?, isLock: Boolean) {
        val title = parseString(inTitle)
        val itself = parseString(inItself)
        if (
            (inTitle == note?.titleNote
                    && inItself == note?.itselfNote
                    && isLock == note?.isLocked
                    && colorIndex == note?.colorIndex)
            || (title.isEmpty()
                    && itself.isEmpty()
                    && haveParsedNote == NOT_HAVE_PARSED_NOTE)
        ) {
            retryNoteListFragment()
        } else {
            note = parseNote(title, itself, isLock)
            _stateNoteEdit.value = NoteEditNote(note = note)
        }
    }

    fun getAddNoteDatabase() {
        addNoteDatabase()
        retryNoteListFragment()
    }

    fun getExtraAddNoteDatabase() {
        addNoteDatabase()
        getNoteListFragment()
    }

    fun getEditNoteDatabase() {
        editNoteDatabase()
        retryNoteListFragment()
    }

    private fun addNoteDatabase() {
        note?.let {
            viewModelScope.launch {
                addNoteEntryUseCase.invoke(it)
            }
        }
        haveParsedNote = NOT_HAVE_PARSED_NOTE
        NotesBackupAgent.requestBackup(backupManager)
    }

    private fun editNoteDatabase() {
        note?.let {
            viewModelScope.launch {
                editNoteEntryUseCase.invoke(it)
            }
        }
        haveParsedNote = NOT_HAVE_PARSED_NOTE
        NotesBackupAgent.requestBackup(backupManager)
    }

    private fun parseNote(inTitle: String, inItself: String, isLock: Boolean): Note {
        var title = inTitle
        var itself = inItself

        if (title.isEmpty()) {
            title = makeTitle(itself)
        } else if (itself.isEmpty()) {
            itself = title
        }
        if (title.length > MAX_TITLE_LENGTH) {
            title = makeTitle(title)
        }
        haveParsedNote = HAVE_PARSED_NOTE
        return Note(
            timeStamp = timeStamp,
            titleNote = title,
            itselfNote = itself,
            colorIndex = colorIndex,
            isLocked = isLock,
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

    fun setTimeStamp(timeStamp: Long) {
        _timeStamp = timeStamp
    }

    fun setColorIndex(index: Int) {
        _stateNoteEdit.value = ColorIndex(index = index)
        colorIndex = index
        setHideColorButtons()
    }

    fun setShowColorButtons() {
        _stateNoteEdit.value = ShowColorButtonsNoteEdit
    }

    fun setHideColorButtons() {
        _stateNoteEdit.value = HideColorButtonsNoteEdit
    }

    fun switchExtraFABs() {
        if (stateShowExtraButton == HIDE_EXTRA_FAB) {
            _stateNoteEdit.value = ShowExtraFABs
            stateShowExtraButton = SHOW_EXTRA_FAB
        } else {
            _stateNoteEdit.value = HideExtraFABs
            stateShowExtraButton = HIDE_EXTRA_FAB
        }
    }

    fun hideExtraFABs() {
        _stateNoteEdit.value = HideExtraFABs
        stateShowExtraButton = HIDE_EXTRA_FAB
    }

    fun retryNoteListFragment() {
        _stateNoteEdit.value = RetryNoteListFragment
    }

    fun getNoteListFragment() {
        _stateNoteEdit.value = GetNoteListFragment
    }

    fun setNoteTitle(title: String) {
        noteTitle = title
    }

    fun setNoteItself(itself: String) {
        noteItself = itself
    }

    companion object {
        const val MAX_TITLE_LENGTH = 25
        private const val SHOW_EXTRA_FAB = "SHOW_EXTRA_FAB"
        private const val HIDE_EXTRA_FAB = "HIDE_EXTRA_FAB"
        private const val HAVE_PARSED_NOTE = "HAVE_PARSED_NOTE"
        private const val NOT_HAVE_PARSED_NOTE = "NOT_HAVE_PARSED_NOTE"
        private const val NOTE_UNLOCK = false
        private const val NOTE_UNSELECTED = false
        const val EMPTY_STRING = ""
    }

}