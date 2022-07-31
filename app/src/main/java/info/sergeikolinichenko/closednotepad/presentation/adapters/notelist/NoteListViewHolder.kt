package info.sergeikolinichenko.closednotepad.presentation.adapters.notelist

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import info.sergeikolinichenko.closednotepad.R

class NoteListViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val tvNoteDate: TextView = view.findViewById(R.id.tv_note_date)
    val tvNoteTime: TextView = view.findViewById(R.id.tv_note_time)
    val tvNoteTitle: TextView = view.findViewById(R.id.tv_note_title)
    val isLocked: AppCompatImageView = view.findViewById(R.id.iv_note_lock)
    val cvNote: MaterialCardView = view.findViewById(R.id.cv_note)
}