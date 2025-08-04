package info.sergeikolinichenko.closednotepad.presentation.di

import androidx.fragment.app.Fragment
import dagger.BindsInstance
import dagger.Subcomponent
import info.sergeikolinichenko.closednotepad.presentation.screens.NoteEditFragment
import info.sergeikolinichenko.closednotepad.presentation.screens.NoteListFragment
import info.sergeikolinichenko.closednotepad.presentation.screens.NoteSearchFragment

/** Created by Sergei Kolinichenko on 25.11.2022 at 22:42 (GMT+3) **/

@Subcomponent
interface FragmentComponent {

    fun inject(fragment: NoteEditFragment)
    fun inject(fragment: NoteListFragment)
    fun inject(fragment: NoteSearchFragment)

    @Subcomponent.Factory
    interface Factory {
        fun create(
            @BindsInstance fragment: Fragment
        ): FragmentComponent
    }
}