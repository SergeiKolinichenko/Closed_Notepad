package info.sergeikolinichenko.closednotepad.presentation.viewmodels

import android.app.backup.BackupManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import info.sergeikolinichenko.closednotepad.presentation.stateful.*
import info.sergeikolinichenko.closednotepad.presentation.utils.NotesBackupAgent
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefAutoDelReNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefColorIndexUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.SetPrefAutoDelReNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.SetPrefColorIndexUseCase
import javax.inject.Inject

class ViewModelNotesMenu @Inject constructor(
    private val setPrefColorIndex: SetPrefColorIndexUseCase,
    private val getPrefColorIndex: GetPrefColorIndexUseCase,
    private val setPrefAutoDelReNote: SetPrefAutoDelReNoteUseCase,
    private val getPrefAutoDelReNote: GetPrefAutoDelReNoteUseCase,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _stateNotesMenu = MutableLiveData<StateNotesMenu>()
    val stateNotesMenu: LiveData<StateNotesMenu>
        get() = _stateNotesMenu

//    private var _showDaySetButtons = MutableLiveData(false)
//    val showDaySetButtons: LiveData<Boolean>
//        get() = _showDaySetButtons

    init {
        _stateNotesMenu.value = DefaultColorIndex(index = getPrefColorIndex.invoke())
        _stateNotesMenu.value = DaysBeforeDelete(days = getPrefAutoDelReNote.invoke())
    }

    fun setDefaultColor(color: Int) {
        setPrefColorIndex.invoke(color)
        _stateNotesMenu.value = DefaultColorIndex(index = getPrefColorIndex.invoke())
        _stateNotesMenu.value = HideColorButtonsNotesMenu
        NotesBackupAgent.requestBackup(backupManager)
    }

    fun setDaysBeforeDelete(days: Int) {
        setPrefAutoDelReNote.invoke(days)
        _stateNotesMenu.value = DaysBeforeDelete(days = getPrefAutoDelReNote.invoke())
        _stateNotesMenu.value = HideSetDaysButtons
        NotesBackupAgent.requestBackup(backupManager)
    }

    fun showSetDaysButton() {
        _stateNotesMenu.value = ShowSetDaysButtons
    }

    fun showColorButtons() {
        _stateNotesMenu.value = ShowColorButtonsNotesMenu
    }
}