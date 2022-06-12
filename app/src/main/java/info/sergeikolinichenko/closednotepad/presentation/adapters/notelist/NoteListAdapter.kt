package info.sergeikolinichenko.closednotepad.presentation.adapters.notelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.models.NoteEntry

class NoteListAdapter: ListAdapter<NoteEntry, NoteListViewHolder>(NoteEntryDiffCallback()) {

    var onEntryClick: ((NoteEntry) -> Unit)? = null

//    var noteList = listOf<NoteEntry>()
//    set(value) {
//        field = value
//        notifyDataSetChanged()
//    }

    //var count = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteListViewHolder {
        //Log.d("MyLog", "onCreateViewHolder ${++count}")

        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.note_list_fragment_item, parent, false
        )
        return NoteListViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteListViewHolder, position: Int) {
        val entry = getItem(position)
        with(holder) {
            tVTitleEntryNote.text = entry.titleEntry
            cvEntryNote.setCardBackgroundColor(entry.colorIndex)
            if (entry.isLocked) {
                isLocked.visibility = View.VISIBLE
            } else {
                isLocked.visibility = View.INVISIBLE
            }

            itemView.setOnClickListener {
                onEntryClick?.invoke(entry)
            }
        }
    }
}