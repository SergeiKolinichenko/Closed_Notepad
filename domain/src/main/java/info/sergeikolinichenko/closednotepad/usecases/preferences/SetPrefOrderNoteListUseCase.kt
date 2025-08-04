package info.sergeikolinichenko.closednotepad.usecases.preferences

import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepository
import javax.inject.Inject

class SetPrefOrderNoteListUseCase @Inject constructor(
    private val prefRepository: PreferencesRepository
    ) {

    operator fun invoke(order: String) {
        prefRepository.setPrefOrderNoteList(order)
    }
}