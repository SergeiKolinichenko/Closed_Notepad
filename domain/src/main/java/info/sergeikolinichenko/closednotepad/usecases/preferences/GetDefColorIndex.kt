package info.sergeikolinichenko.closednotepad.usecases.preferences

import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepository

class GetDefColorIndex(private val prefRepository: PreferencesRepository) {
    operator fun invoke(): Int {
        return prefRepository.getDefColorIndex()
    }
}