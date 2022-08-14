package info.sergeikolinichenko.closednotepad.presentation.viewmodels.settins

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefColorIndexUseCase
import info.sergeikolinichenko.closednotepad.usecases.preferences.SetPrefColorIndexUseCase

class ViewModelSettings(
    private val setPrefColorIndex: SetPrefColorIndexUseCase,
    private val getPrefColorIndex: GetPrefColorIndexUseCase
): ViewModel() {

    private val _defaultColorIndex = MutableLiveData<Int>()
    val defaultColorIndex: LiveData<Int>
    get() = _defaultColorIndex

    init {
        _defaultColorIndex.value = getPrefColorIndex.invoke()
    }

    fun setDefaultColor(color: Int) {
        setPrefColorIndex.invoke(color)
        _defaultColorIndex.value = getPrefColorIndex.invoke()
    }
}