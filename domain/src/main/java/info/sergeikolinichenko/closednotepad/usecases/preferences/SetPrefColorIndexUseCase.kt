package info.sergeikolinichenko.closednotepad.usecases.preferences

import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepository
import javax.inject.Inject

class SetPrefColorIndexUseCase @Inject constructor(
    private val prefRepository: PreferencesRepository
    ) {

    operator fun invoke(index: Int) {
        prefRepository.setPrefColorIndex(index)
    }
}