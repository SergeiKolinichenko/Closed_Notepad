package info.sergeikolinichenko.closednotepad.presentation.adapters.trashcanlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.ListAdapter
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.models.RemovedNote
import info.sergeikolinichenko.closednotepad.presentation.adapters.TrashCanListDiffCallback
import info.sergeikolinichenko.closednotepad.presentation.utils.NoteColors
import info.sergeikolinichenko.closednotepad.presentation.utils.TimeUtils

class TrashCanAdapter: ListAdapter<RemovedNote,
        TrashCanListViewHolder>(TrashCanListDiffCallback()) {

    var onReNoteClick: ((RemovedNote) -> Unit)? = null
    var onReNoteLongClick: ((RemovedNote) -> Unit)? = null
    var isNight: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            TrashCanListViewHolder {
        val layoutType = when (viewType) {
            SELECTED_ENTRY -> R.layout.selected_removed_note_list_item
            UNSELECTED_ENTRY -> R.layout.removed_note_list_item
            else -> throw RuntimeException("Unknown view type $viewType")
        }
        val view = LayoutInflater.from(parent.context).inflate(
            layoutType, parent, false
        )
        return TrashCanListViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrashCanListViewHolder, position: Int) {
        val removedNote = getItem(position)
        val colorBack = if (isNight)
            NoteColors.noteColor[NoteColors.DARK_COLOR][removedNote.colorIndex]
        else
            NoteColors.noteColor[NoteColors.LIGHT_COLOR][removedNote.colorIndex]

        val imgLock = if (isNight) R.drawable.ic_lock_white_36dp
        else R.drawable.ic_lock_black_36dp

        with(holder) {
            tvReNoteDateCreate.text = TimeUtils.getDate(removedNote.timeStamp)
            tvReNoteDateRemoved.text = TimeUtils.getDate(removedNote.timeRemove)
            tvReNoteTitle.text = removedNote.titleNote
            cvReNote.setBackgroundResource(colorBack)
            isReLocked.setImageResource(imgLock)

            if (removedNote.isLocked) isReLocked.visibility = View.VISIBLE
            else isReLocked.visibility = View.INVISIBLE

            itemView.setOnClickListener {
                onReNoteClick?.invoke(removedNote)
            }

            itemView.setOnLongClickListener {
                onReNoteLongClick?.invoke(removedNote)
                true
            }
        }
        holder.cvReNote.animation = AnimationUtils.loadAnimation(
            holder.itemView.context, R.anim.anim_trash_can_list
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