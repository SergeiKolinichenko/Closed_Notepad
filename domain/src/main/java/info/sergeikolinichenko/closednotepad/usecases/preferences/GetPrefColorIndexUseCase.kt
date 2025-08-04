package info.sergeikolinichenko.closednotepad.usecases.preferences

import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepository
import javax.inject.Inject

class GetPrefColorIndexUseCase @Inject constructor(
    private val prefRepository: PreferencesRepository
    ) {
    operator fun invoke(): Int {
        return prefRepository.getPrefColorIndex()
    }
}