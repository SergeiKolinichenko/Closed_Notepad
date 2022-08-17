package info.sergeikolinichenko.closednotepad.utils

import info.sergeikolinichenko.closednotepad.dbmodels.RemovedNoteDbModel
import info.sergeikolinichenko.closednotepad.models.RemovedNote

class RemovedNoteMapper {

    fun mapEntityToDbModel(removedNote: RemovedNote) = RemovedNoteDbModel(
        timeStamp = removedNote.timeStamp,
        titleEntry = removedNote.titleEntry,
        itselfEntry = removedNote.itselfEntry,
        colorIndex = removedNote.colorIndex,
        isLocked = removedNote.isLocked
    )

    fun mapDbModelToEntity(removedNoteDbModel: RemovedNoteDbModel) = RemovedNote(
        timeStamp = removedNoteDbModel.timeStamp,
        titleEntry = removedNoteDbModel.titleEntry,
        itselfEntry = removedNoteDbModel.itselfEntry,
        colorIndex = removedNoteDbModel.colorIndex,
        isLocked = removedNoteDbModel.isLocked
    )
}