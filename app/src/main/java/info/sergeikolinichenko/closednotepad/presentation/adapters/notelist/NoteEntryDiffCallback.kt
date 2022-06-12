package info.sergeikolinichenko.closednotepad.presentation.adapters.notelist

import androidx.recyclerview.widget.DiffUtil
import info.sergeikolinichenko.closednotepad.models.NoteEntry

class NoteEntryDiffCallback: DiffUtil.ItemCallback<NoteEntry>() {

    override fun areItemsTheSame(oldItem: NoteEntry, newItem: NoteEntry): Boolean {
        return oldItem.timeStamp == newItem.timeStamp
    }

    override fun areContentsTheSame(oldItem: NoteEntry, newItem: NoteEntry): Boolean {
        return oldItem == newItem
    }
}