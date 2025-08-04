package info.sergeikolinichenko.closednotepad.workers

import android.content.Context
import androidx.work.*
import info.sergeikolinichenko.closednotepad.database.NotesDao
import info.sergeikolinichenko.closednotepad.models.RemovedNote
import info.sergeikolinichenko.closednotepad.usecases.preferences.GetPrefAutoDelReNoteUseCase
import info.sergeikolinichenko.closednotepad.utils.RemovedNoteMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/** Created by Sergei Kolinichenko on 03.12.2022 at 21:42 (GMT+3) **/

class RemovedNoteListWorker(
    context: Context,
    workerParameters: WorkerParameters,
    private val getPrefDayBeforeDelete: GetPrefAutoDelReNoteUseCase,
    private val notesDao: NotesDao,
    private val mapper: RemovedNoteMapper
) : Worker(context, workerParameters) {

    private val coroutineScope= CoroutineScope(Dispatchers.Default)

    override fun doWork(): Result {
        val days = getPrefDayBeforeDelete.invoke()
        val list: List<RemovedNote> = mapper.mapListDbModelToListEntity(notesDao.getRemovedNoteList())

        if ( days > 0 && list.isNotEmpty()) {
            for (item in list) {
                if ( (item.timeStamp).getDiffDays() > days) {
                    coroutineScope.launch {
                        notesDao.deleteRemovedNote(item.timeStamp)
                    }
                }
            }
        }
        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
        coroutineScope.cancel()
    }

    private fun Long.getDiffDays(): Int {
        val oneDay = 1000 * 60 * 60 * 24
        val result = ((Date().time - this) / oneDay) + 1
        return result.toInt()
    }

    companion object {

        const val WORKER_NAME = "RemovedNoteListWorker"

        fun makeRequest(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<RemovedNoteListWorker>().build()
        }
    }

    class Factory @Inject constructor(
        private val getPrefDayBeforeDelete: GetPrefAutoDelReNoteUseCase,
        private val notesDao: NotesDao,
        private val mapper: RemovedNoteMapper
    ) : ChildWorkerFactory {
        override fun create(
            context: Context,
            workerParameters: WorkerParameters
        ): ListenableWorker {
            return RemovedNoteListWorker(
                context,
                workerParameters,
                getPrefDayBeforeDelete,
                notesDao,
                mapper
            )
        }
    }
}