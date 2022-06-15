package info.sergeikolinichenko.closednotepad.presentation.adapters.notelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.models.NoteEntry
import info.sergeikolinichenko.closednotepad.presentation.utils.TimeUtils

class NoteListAdapter : ListAdapter<NoteEntry, NoteListViewHolder>(NoteEntryDiffCallback()) {

    var onEntryClick: ((NoteEntry) -> Unit)? = null
    var onEntryLongClick: ((NoteEntry) -> Unit)? = null

//    var noteList = listOf<NoteEntry>()
//    set(value) {
//        field = value
//        notifyDataSetChanged()
//    }

    //var count = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteListViewHolder {
        //Log.d("MyLog", "onCreateViewHolder ${++count}")
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
        with(holder) {
            tvDateEntryNote.text = TimeUtils.getDate(entry.timeStamp)
            tvTimeEntryNote.text = TimeUtils.getTime(entry.timeStamp)
            tvTitleEntryNote.text = entry.titleEntry
            cvEntryNote.setCardBackgroundColor(entry.colorIndex)
            if (entry.isLocked) {
                isLocked.visibility = View.VISIBLE
            } else {
                isLocked.visibility = View.INVISIBLE
            }

            itemView.setOnClickListener {
                onEntryClick?.invoke(entry)
            }

            itemView.setOnLongClickListener {
                onEntryLongClick?.invoke(entry)
                true
            }
        }
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