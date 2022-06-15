package info.sergeikolinichenko.closednotepad.presentation.adapters.notelist

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import info.sergeikolinichenko.closednotepad.R

class NoteListViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
    val tvDateEntryNote = view.findViewById<TextView>(R.id.tv_date_entry_note)
    val tvTimeEntryNote = view.findViewById<TextView>(R.id.tv_time_entry_note)
    val tvTitleEntryNote = view.findViewById<TextView>(R.id.tv_title_entry_note)
    val isLocked = view.findViewById<AppCompatImageView>(R.id.iv_lock_entry_note)
    val cvEntryNote = view.findViewById<CardView>(R.id.cv_entry_note)
}