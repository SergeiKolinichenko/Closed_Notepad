package info.sergeikolinichenko.closednotepad.usecases.preferences

import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepository
import javax.inject.Inject

class GetPrefOrderNoteListUseCase @Inject constructor(
    private val prefRepository: PreferencesRepository
    ) {

    operator fun invoke(): String {
        return prefRepository.getPrefOrderNoteList()
    }
}