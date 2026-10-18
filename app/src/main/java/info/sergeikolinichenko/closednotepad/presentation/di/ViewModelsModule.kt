package info.sergeikolinichenko.closednotepad.presentation.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import info.sergeikolinichenko.closednotepad.presentation.ui.editor.EditorViewModel
import info.sergeikolinichenko.closednotepad.presentation.ui.home.HomeViewModel
import info.sergeikolinichenko.closednotepad.presentation.ui.search.SearchViewModel
import info.sergeikolinichenko.closednotepad.presentation.ui.settings.SettingsViewModel
import info.sergeikolinichenko.closednotepad.presentation.ui.trash.TrashViewModel

/** Created by Sergei Kolinichenko on 23.11.2022 at 20:13 (GMT+3) **/

@Module
interface ViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    fun bindHomeViewModel(viewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(EditorViewModel::class)
    fun bindEditorViewModel(viewModel: EditorViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    fun bindSearchViewModel(viewModel: SearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TrashViewModel::class)
    fun bindTrashViewModel(viewModel: TrashViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    fun bindSettingsViewModel(viewModel: SettingsViewModel): ViewModel
}
