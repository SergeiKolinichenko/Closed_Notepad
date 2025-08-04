package info.sergeikolinichenko.closednotepad.presentation.adapters.trashcanlist

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import info.sergeikolinichenko.closednotepad.R

class TrashCanListViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val tvReNoteDateCreate: TextView = view.findViewById(R.id.tv_removed_note_date_create)
    val tvReNoteDateRemoved: TextView = view.findViewById(R.id.tv_removed_note_date_removed)
    val tvReNoteTitle: TextView = view.findViewById(R.id.tv_removed_note_title)
    val isReLocked: AppCompatImageView = view.findViewById(R.id.iv_removed_note_lock)
    val cvReNote: MaterialCardView = view.findViewById(R.id.cv_removed_note)
}