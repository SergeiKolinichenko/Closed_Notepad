package info.sergeikolinichenko.closednotepad.presentation.shortcuts

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.models.NoteType

// Home screen widget: a new text note, to-do list or shopping list in one tap.
// It shows no note content, so locked notes stay hidden.
class NewNoteWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        val views = RemoteViews(context.packageName, R.layout.widget_new_note).apply {
            setOnClickPendingIntent(R.id.widget_new_text, NewNoteIntents.pendingIntent(context, NoteType.TEXT))
            setOnClickPendingIntent(R.id.widget_new_checklist, NewNoteIntents.pendingIntent(context, NoteType.CHECKLIST))
            setOnClickPendingIntent(R.id.widget_new_shopping, NewNoteIntents.pendingIntent(context, NoteType.SHOPPING))
        }
        manager.updateAppWidget(widgetIds, views)
    }
}
