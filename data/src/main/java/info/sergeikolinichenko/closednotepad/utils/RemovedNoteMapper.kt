package info.sergeikolinichenko.closednotepad.utils

import info.sergeikolinichenko.closednotepad.dbmodels.RemovedNoteDbModel
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.RemovedNote
import javax.inject.Inject

class RemovedNoteMapper @Inject constructor() {

    fun mapEntityToDbModel(removedNote: RemovedNote) = RemovedNoteDbModel(
        timeStamp = removedNote.timeStamp,
        timeRemove = removedNote.timeRemove,
        titleEntry = removedNote.titleNote,
        itselfEntry = removedNote.itselfNote,
        colorIndex = removedNote.colorIndex,
        isLocked = removedNote.isLocked
    )

    fun mapDbModelToEntity(removedNoteDbModel: RemovedNoteDbModel) = RemovedNote(
        timeStamp = removedNoteDbModel.timeStamp,
        timeRemove = removedNoteDbModel.timeRemove,
        titleNote = removedNoteDbModel.titleEntry,
        itselfNote = removedNoteDbModel.itselfEntry,
        colorIndex = removedNoteDbModel.colorIndex,
        isLocked = removedNoteDbModel.isLocked,
        isSelected = false
    )

    fun mapRemovedNoteToNote(removedNote: RemovedNote) = Note (
        timeStamp = removedNote.timeStamp,
        titleNote = removedNote.titleNote,
        itselfNote = removedNote.itselfNote,
        colorIndex = removedNote.colorIndex,
        isLocked = removedNote.isLocked,
        isSelected = false
            )

    fun mapListDbModelToListEntity(list: List<RemovedNoteDbModel>) = list.map {
        mapDbModelToEntity(it)
    }
}