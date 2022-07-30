package info.sergeikolinichenko.closednotepad.presentation.adapters.notelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.ListAdapter
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.models.Note
import info.sergeikolinichenko.closednotepad.presentation.utils.EntriesColors
import info.sergeikolinichenko.closednotepad.presentation.utils.TimeUtils

class NoteListAdapter : ListAdapter<Note, NoteListViewHolder>(NoteListDiffCallback()) {

    var onEntryClick: ((Note) -> Unit)? = null
    var onEntryLongClick: ((Note) -> Unit)? = null
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
        val entry = getItem(position)
        val colorBack = if (isNight)
            EntriesColors.entriesColor[EntriesColors.DARK_COLOR][entry.colorIndex]
        else
            EntriesColors.entriesColor[EntriesColors.LIGHT_COLOR][entry.colorIndex]

        val imgLock = if (isNight) R.drawable.ic_lock_white_36dp
        else R.drawable.ic_lock_black_36dp

        with(holder) {
            tvDateEntryNote.text = TimeUtils.getDate(entry.timeStamp)
            tvTimeEntryNote.text = TimeUtils.getTime(entry.timeStamp)
            tvTitleEntryNote.text = entry.titleNote
            cvEntryNote.setBackgroundResource(colorBack)
            isLocked.setImageResource(imgLock)

            if (entry.isLocked) isLocked.visibility = View.VISIBLE
            else isLocked.visibility = View.INVISIBLE

            itemView.setOnClickListener {
                onEntryClick?.invoke(entry)
            }

            itemView.setOnLongClickListener {
                onEntryLongClick?.invoke(entry)
                true
            }
        }
        holder.cvEntryNote.animation = AnimationUtils.loadAnimation(
            holder.itemView.context, R.anim.anim_note_entry
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