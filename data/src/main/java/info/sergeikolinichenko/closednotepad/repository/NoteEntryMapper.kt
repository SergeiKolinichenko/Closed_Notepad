package info.sergeikolinichenko.closednotepad.repository

import info.sergeikolinichenko.closednotepad.dbmodels.NoteEntryDbModel
import info.sergeikolinichenko.closednotepad.models.NoteEntry

class NoteEntryMapper {

    fun mapEntityToDbModel(noteEntry: NoteEntry) = NoteEntryDbModel(
        timeStamp = noteEntry.timeStamp,
        titleEntry = noteEntry.titleEntry,
        itselfEntry = noteEntry.itselfEntry,
        colorIndex = noteEntry.colorIndex,
        isLocked = noteEntry.isLocked
    )

    fun mapDbModelToEntity(noteEntryDbModel: NoteEntryDbModel) = NoteEntry(
        timeStamp = noteEntryDbModel.timeStamp,
        titleEntry = noteEntryDbModel.titleEntry,
        itselfEntry = noteEntryDbModel.itselfEntry,
        colorIndex = noteEntryDbModel.colorIndex,
        isLocked = noteEntryDbModel.isLocked,
        isSelected = false
    )

    fun mapListDbModelToListEntity(list: List<NoteEntryDbModel>) = list.map {
        mapDbModelToEntity(it)
    }
}