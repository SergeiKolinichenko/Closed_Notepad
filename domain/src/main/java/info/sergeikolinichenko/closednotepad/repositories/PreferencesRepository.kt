package info.sergeikolinichenko.closednotepad.repositories

import info.sergeikolinichenko.closednotepad.models.NotesLayout
import info.sergeikolinichenko.closednotepad.models.Settings
import info.sergeikolinichenko.closednotepad.models.SortOrder
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    val settings: Flow<Settings>

    fun current(): Settings

    fun setSortOrder(order: SortOrder)

    fun setDefaultColorIndex(index: Int)

    fun setDaysBeforeDeletion(days: Int)

    fun setLayout(layout: NotesLayout)
}
