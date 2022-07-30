package info.sergeikolinichenko.closednotepad.repository

import info.sergeikolinichenko.closednotepad.dbmodels.NoteDbModel
import info.sergeikolinichenko.closednotepad.models.Note

class NoteMapper {

    fun mapEntityToDbModel(note: Note) = NoteDbModel(
        timeStamp = note.timeStamp,
        titleNote = note.titleNote,
        itselfNote = note.itselfNote,
        colorIndex = note.colorIndex,
        isLocked = note.isLocked
    )

    fun mapDbModelToEntity(noteDbModel: NoteDbModel) = Note(
        timeStamp = noteDbModel.timeStamp,
        titleNote = noteDbModel.titleNote,
        itselfNote = noteDbModel.itselfNote,
        colorIndex = noteDbModel.colorIndex,
        isLocked = noteDbModel.isLocked,
        isSelected = false
    )

    fun mapListDbModelToListEntity(list: List<NoteDbModel>) = list.map {
        mapDbModelToEntity(it)
    }
}