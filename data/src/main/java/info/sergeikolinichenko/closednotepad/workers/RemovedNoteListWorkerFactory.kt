package info.sergeikolinichenko.closednotepad.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import javax.inject.Inject
import javax.inject.Provider

/** Created by Sergei Kolinichenko on 04.12.2022 at 17:34 (GMT+3) **/

class RemovedNoteListWorkerFactory @Inject constructor(
    private val workerProvides:
    @JvmSuppressWildcards Map<Class<out ListenableWorker>, Provider<ChildWorkerFactory>>
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when(workerClassName) {
            RemovedNoteListWorker::class.qualifiedName -> {
                val childWorkerFactory = workerProvides[RemovedNoteListWorker::class.java]?.get()
                return childWorkerFactory?.create( appContext, workerParameters )
            }
            else -> null
        }
    }
}