package info.sergeikolinichenko.closednotepad.presentation.viewmodels.trashcanlist

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.sergeikolinichenko.closednotepad.repositories.RemovedNoteRepositoryImpl
import info.sergeikolinichenko.closednotepad.usecases.trashcan.GetListRemovedNoteUseCase

class ViewModelTrashCanListFactory(application: Application): ViewModelProvider.Factory {

    private val repository = RemovedNoteRepositoryImpl(application)

    private val getRemovedNoteList = GetListRemovedNoteUseCase(repository)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if(modelClass.isAssignableFrom(ViewModelTrashCanList::class.java)) {
            @Suppress("UNCHECKED_CAST")
            ViewModelTrashCanList(
                getRemovedNoteList
            ) as T
        } else {
            throw RuntimeException("Unknown view Model class $modelClass")
        }
    }
}