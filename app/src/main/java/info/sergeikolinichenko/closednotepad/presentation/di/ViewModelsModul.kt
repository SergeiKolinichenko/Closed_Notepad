package info.sergeikolinichenko.closednotepad.presentation.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNoteEdit
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNoteList
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNoteSearch
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNotesMenu
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelNoteView
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelTrashCanList
import info.sergeikolinichenko.closednotepad.presentation.viewmodels.ViewModelTrashCanView

/** Created by Sergei Kolinichenko on 23.11.2022 at 20:13 (GMT+3) **/

@Module
interface ViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(ViewModelNoteEdit::class)
    fun bindViewModelNoteEdit(viewModel: ViewModelNoteEdit): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ViewModelNoteList::class)
    fun bindViewModelNoteList(viewModel: ViewModelNoteList): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ViewModelNoteSearch::class)
    fun bindViewModelNoteSearch(viewModel: ViewModelNoteSearch): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ViewModelNotesMenu::class)
    fun bindViewModelNotesMenu(viewModel: ViewModelNotesMenu): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ViewModelNoteView::class)
    fun bindViewModelNoteView(viewModel: ViewModelNoteView): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ViewModelTrashCanList::class)
    fun bindViewModelTrashCanList(viewModel: ViewModelTrashCanList): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ViewModelTrashCanView::class)
    fun bindViewModelTrashCanView(viewModel: ViewModelTrashCanView): ViewModel
}