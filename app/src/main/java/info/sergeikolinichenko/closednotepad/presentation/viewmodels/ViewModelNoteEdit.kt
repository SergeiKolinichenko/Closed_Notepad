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

/**
ViewModel of NoteEditFragment
create 28.07.2022 by Sergei Kolinichenko
 **/

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
    private var stateShowExtraButton: String = HIDE_EXTRA_BUTTON

    fun getNoteEntry() {
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

        if (title.isEmpty() && itself.isEmpty()) retryNoteListFragment()
        else note = parseNote(title, itself, isLock)
    }

    fun editNote(inTitle: String?, inItself: String?, isLock: Boolean) {
        val title = parseString(inTitle)
        val itself = parseString(inItself)
        if (
            (inTitle == note?.titleNote
                    && inItself == note?.itselfNote
                    && isLock == note?.isLocked
                    && colorIndex == note?.colorIndex)
            || (title.isEmpty() && itself.isEmpty())
        ) {
            retryNoteListFragment()
        } else {
            note = parseNote(title, itself, isLock)
        }
    }

    fun addNoteDatabase() {
        note?.let {
            viewModelScope.launch {
                addNoteEntryUseCase.invoke(it)
            }
        }
        NotesBackupAgent.requestBackup(backupManager)
        retryNoteListFragment()
    }

    fun editNoteDatabase() {
        note?.let {
            viewModelScope.launch {
                editNoteEntryUseCase.invoke(it)
            }
        }
        NotesBackupAgent.requestBackup(backupManager)
        retryNoteListFragment()
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
        if (stateShowExtraButton != SHOW_EXTRA_BUTTON) {
            _stateNoteEdit.value = ShowExtraFABs
            stateShowExtraButton = SHOW_EXTRA_BUTTON
        } else {
            _stateNoteEdit.value = HideExtraFABs
            stateShowExtraButton = HIDE_EXTRA_BUTTON
        }
    }

    fun hideExtraFABs() {
        _stateNoteEdit.value = HideExtraFABs
    }

    fun retryNoteListFragment() {
        _stateNoteEdit.value = RetryNoteListFragment
    }

    companion object {
        private const val MAX_TITLE_LENGTH = 25
        private const val SHOW_EXTRA_BUTTON = "SHOW_EXTRA_BUTTON"
        private const val HIDE_EXTRA_BUTTON = "HIDE_EXTRA_BUTTON"
    }

}