package info.sergeikolinichenko.closednotepad.utils

import info.sergeikolinichenko.closednotepad.dbmodels.RemovedNoteDbModel
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.models.RemovedNote

class RemovedNoteMapper {

    fun mapEntityToDbModel(removedNote: RemovedNote) = RemovedNoteDbModel(
        timeStamp = removedNote.timeStamp,
        titleEntry = removedNote.titleNote,
        itselfEntry = removedNote.itselfNote,
        colorIndex = removedNote.colorIndex,
        isLocked = removedNote.isLocked
    )

    private fun mapDbModelToEntity(removedNoteDbModel: RemovedNoteDbModel) = RemovedNote(
        timeStamp = removedNoteDbModel.timeStamp,
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