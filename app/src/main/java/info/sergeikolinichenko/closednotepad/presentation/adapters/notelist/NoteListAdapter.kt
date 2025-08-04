package info.sergeikolinichenko.closednotepad.presentation.adapters.notelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.ListAdapter
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.presentation.adapters.NoteListDiffCallback
import info.sergeikolinichenko.closednotepad.presentation.utils.NoteColors
import info.sergeikolinichenko.closednotepad.presentation.utils.TimeUtils

class NoteListAdapter : ListAdapter<Note, NoteListViewHolder>(NoteListDiffCallback()) {

    var onNoteClick: ((Note) -> Unit)? = null
    var onNoteLongClick: ((Note) -> Unit)? = null
    var isNight: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteListViewHolder {

        val layoutType = when (viewType) {
            SELECTED_ENTRY -> R.layout.selected_note_list_item
            UNSELECTED_ENTRY -> R.layout.note_list_item
            else -> throw RuntimeException("Unknown view type $viewType")
        }
        val view = LayoutInflater.from(parent.context).inflate(
            layoutType, parent, false
        )
        return NoteListViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteListViewHolder, position: Int) {
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

            itemView.setOnLongClickListener {
                onNoteLongClick?.invoke(note)
                true
            }
        }
        holder.cvNote.animation = AnimationUtils.loadAnimation(
            holder.itemView.context, R.anim.anim_note_list
        )
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isSelected) SELECTED_ENTRY
        else UNSELECTED_ENTRY
    }

    companion object {
        private const val SELECTED_ENTRY = 1
        private const val UNSELECTED_ENTRY = 0
    }
}