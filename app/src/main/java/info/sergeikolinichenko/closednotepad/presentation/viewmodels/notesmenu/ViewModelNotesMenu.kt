package info.sergeikolinichenko.closednotepad.presentation.viewmodels.notesmenu

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefAutoDelReNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefColorIndexUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.SetPrefAutoDelReNoteUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.SetPrefColorIndexUseCase

class ViewModelNotesMenu(
    private val setPrefColorIndex: SetPrefColorIndexUseCase,
    private val getPrefColorIndex: GetPrefColorIndexUseCase,
    private val setPrefAutoDelReNote: SetPrefAutoDelReNoteUseCase,
    getPrefAutoDelReNote: GetPrefAutoDelReNoteUseCase
): ViewModel() {

    private val _defaultColorIndex = MutableLiveData<Int>()
    val defaultColorIndex: LiveData<Int>
    get() = _defaultColorIndex

    private val _daysBeforeDelete = MutableLiveData<Int>()
    val daysBeforeDelete: LiveData<Int>
        get() = _daysBeforeDelete

    private var _showColorButtons = MutableLiveData(false)
    val showColorButtons: LiveData<Boolean>
    get() = _showColorButtons

    private var _showDaySetButtons = MutableLiveData(false)
    val showDaySetButtons: LiveData<Boolean>
        get() = _showDaySetButtons

    init {
        _defaultColorIndex.value = getPrefColorIndex.invoke()
        _daysBeforeDelete.value = getPrefAutoDelReNote.invoke()
    }

    fun setDefaultColor(color: Int) {
        setPrefColorIndex.invoke(color)
        _defaultColorIndex.value = getPrefColorIndex.invoke()
        _showColorButtons.value = false
    }

    fun setDaysBeforeDelete(days: Int) {
        setPrefAutoDelReNote.invoke(days)
        _daysBeforeDelete.value = days
        _showDaySetButtons.value = false
    }

    fun showSetDaysButton() {
        _showDaySetButtons.value = true
    }

    fun showColorButtons() {
        _showColorButtons.value = true
    }
}