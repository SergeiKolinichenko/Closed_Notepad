package info.sergeikolinichenko.closednotepad.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import info.sergeikolinichenko.closednotepad.repositories.NotesRepository
import info.sergeikolinichenko.closednotepad.repositories.PreferencesRepository
import info.sergeikolinichenko.closednotepad.usecases.TrashPolicy
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** Created by Sergei Kolinichenko on 03.12.2022 at 21:42 (GMT+3) **/

// Deletes notes that have been in the trash longer than the user allows
class RemovedNoteListWorker(
    context: Context,
    workerParameters: WorkerParameters,
    private val notesRepository: NotesRepository,
    private val preferencesRepository: PreferencesRepository
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val days = preferencesRepository.current().daysBeforeDeletion
        TrashPolicy.deleteBefore(System.currentTimeMillis(), days)?.let { time ->
            notesRepository.deleteTrashedBefore(time)
        }
        return Result.success()
    }

    companion object {

        const val WORKER_NAME = "RemovedNoteListWorker"

        fun schedule(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<RemovedNoteListWorker>(1, TimeUnit.DAYS).build()
            workManager.enqueueUniquePeriodicWork(
                WORKER_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }

    class Factory @Inject constructor(
        private val notesRepository: NotesRepository,
        private val preferencesRepository: PreferencesRepository
    ) : ChildWorkerFactory {
        override fun create(
            context: Context,
            workerParameters: WorkerParameters
        ): ListenableWorker = RemovedNoteListWorker(
            context,
            workerParameters,
            notesRepository,
            preferencesRepository
        )
    }
}
