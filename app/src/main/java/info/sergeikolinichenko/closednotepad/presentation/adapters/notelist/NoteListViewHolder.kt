package info.sergeikolinichenko.closednotepad.presentation.adapters.notelist

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import info.sergeikolinichenko.closednotepad.R

class NoteListViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val tVTitleEntryNote = view.findViewById<TextView>(R.id.tv_title_entry_note)
    val isLocked = view.findViewById<AppCompatImageView>(R.id.iv_lock_entry_note)
    val cvEntryNote = view.findViewById<CardView>(R.id.cv_entry_note)
}