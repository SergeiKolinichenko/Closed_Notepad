package info.sergeikolinichenko.closednotepad.usecases.preferences

import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepository

class SetPrefColorIndexUseCase(private val prefRepository: PreferencesRepository) {

    operator fun invoke(index: Int) {
        prefRepository.setPrefColorIndex(index)
    }
}