package info.sergeikolinichenko.closednotepad.presentation.adapters.notelist

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import info.sergeikolinichenko.closednotepad.R

class NoteListViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val tvDateEntryNote: TextView = view.findViewById(R.id.tv_date_entry_note)
    val tvTimeEntryNote: TextView = view.findViewById(R.id.tv_time_entry_note)
    val tvTitleEntryNote: TextView = view.findViewById(R.id.tv_title_entry_note)
    val isLocked: AppCompatImageView = view.findViewById(R.id.iv_lock_entry_note)
    val cvEntryNote: MaterialCardView = view.findViewById(R.id.cv_entry_note)
}