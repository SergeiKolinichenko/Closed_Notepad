package info.sergeikolinichenko.closednotepad.presentation.adapters

import androidx.recyclerview.widget.DiffUtil
import info.sergeikolinichenko.closednotepad.models.Note

class NoteListDiffCallback: DiffUtil.ItemCallback<Note>() {

    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.timeStamp == newItem.timeStamp
    }

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem == newItem
    }
}