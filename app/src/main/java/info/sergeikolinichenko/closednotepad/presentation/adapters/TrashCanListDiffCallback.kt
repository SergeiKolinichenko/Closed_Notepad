package info.sergeikolinichenko.closednotepad.presentation.adapters

import androidx.recyclerview.widget.DiffUtil
import info.sergeikolinichenko.closednotepad.models.RemovedNote

class TrashCanListDiffCallback: DiffUtil.ItemCallback<RemovedNote>() {

    override fun areItemsTheSame(oldItem: RemovedNote, newItem: RemovedNote): Boolean {
        return oldItem.timeStamp == newItem.timeStamp
    }

    override fun areContentsTheSame(oldItem: RemovedNote, newItem: RemovedNote): Boolean {
        return oldItem == newItem
    }
}