package info.sergeikolinichenko.closednotepad.usecases.preferences

import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepository

class SetPrefAutoDelReNoteUseCase(private val prefRepository: PreferencesRepository) {

    operator fun invoke(days: Int) {
        prefRepository.setPrefAutoDelReNote(days)
    }
}