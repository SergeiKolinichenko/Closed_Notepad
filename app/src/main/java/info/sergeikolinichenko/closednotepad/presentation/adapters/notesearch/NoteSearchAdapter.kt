package info.sergeikolinichenko.closednotepad.presentation.adapters.notesearch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.presentation.adapters.NoteListDiffCallback
import info.sergeikolinichenko.closednotepad.presentation.utils.NoteColors
import info.sergeikolinichenko.closednotepad.presentation.utils.TimeUtils

class NoteSearchAdapter: ListAdapter<Note, NoteSearchViewHolder>(NoteListDiffCallback()) {

    var onNoteClick: ((Note) -> Unit)? = null
    var isNight: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteSearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.note_list_item, parent, false)
        return NoteSearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteSearchViewHolder, position: Int) {
        val note = getItem(position)
        val colorBack = if (isNight)
            NoteColors.noteColor[NoteColors.DARK_COLOR][note.colorIndex]
        else
            NoteColors.noteColor[NoteColors.LIGHT_COLOR][note.colorIndex]

        val imgLock = if (isNight) R.drawable.ic_lock_white_36dp
        else R.drawable.ic_lock_black_36dp

        with(holder) {

            tvNoteDate.text = TimeUtils.getDate(note.timeStamp)
            tvNoteTime.text = TimeUtils.getTime(note.timeStamp)
            tvNoteTitle.text = note.titleNote
            cvNote.setBackgroundResource(colorBack)
            isLocked.setImageResource(imgLock)

            if (note.isLocked) isLocked.visibility = View.VISIBLE
            else isLocked.visibility = View.INVISIBLE

            itemView.setOnClickListener {
                onNoteClick?.invoke(note)
            }
        }
    }

}