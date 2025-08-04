package info.sergeikolinichenko.closednotepad.usecases.preferences

import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepository
import javax.inject.Inject

class SetPrefAutoDelReNoteUseCase @Inject constructor(
    private val prefRepository: PreferencesRepository
    ) {

    operator fun invoke(days: Int) {
        prefRepository.setPrefAutoDelReNote(days)
    }
}