package info.sergeikolinichenko.closednotepad.usecases.preferences

import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepository

class GetPrefColorIndexUseCase(private val prefRepository: PreferencesRepository) {
    operator fun invoke(): Int {
        return prefRepository.getPrefColorIndex()
    }
}