package info.sergeikolinichenko.closednotepad.presentation.viewmodels.trashcanlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import info.sergeikolinichenko.closednotepad.models.RemovedNote
import info.sergeikolinichenko.closednotepad.usecases.trashcan.GetListRemovedNoteUseCase

class ViewModelTrashCanList(
    getRemovedNoteList: GetListRemovedNoteUseCase
): ViewModel() {

    private var _removedNoteList: MutableLiveData<List<RemovedNote>> = getRemovedNoteList.invoke()
    val removedNoteList: LiveData<List<RemovedNote>>
        get() = _removedNoteList

}