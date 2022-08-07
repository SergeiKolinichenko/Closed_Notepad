package info.sergeikolinichenko.closednotepad.usecases.preferences

import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepository

class GetOrderViewNoteList(private val prefRepository: PreferencesRepository) {

    operator fun invoke(): String {
        return prefRepository.getOrderViewNoteList()
    }
}