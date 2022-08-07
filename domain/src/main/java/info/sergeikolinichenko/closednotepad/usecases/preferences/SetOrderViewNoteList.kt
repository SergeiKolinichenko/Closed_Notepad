package info.sergeikolinichenko.closednotepad.usecases.preferences

import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepository

class SetOrderViewNoteList(private val prefRepository: PreferencesRepository) {

    operator fun invoke(order: String) {
        prefRepository.setOrderViewNoteList(order)
    }
}