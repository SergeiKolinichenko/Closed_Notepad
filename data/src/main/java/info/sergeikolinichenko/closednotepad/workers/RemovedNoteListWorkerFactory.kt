package info.sergeikolinichenko.closednotepad.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import javax.inject.Inject
import javax.inject.Provider

/** Created by Sergei Kolinichenko on 04.12.2022 at 17:34 (GMT+3) **/

class RemovedNoteListWorkerFactory @Inject constructor(
    private val workerProviders:
    @JvmSuppressWildcards Map<Class<out ListenableWorker>, Provider<ChildWorkerFactory>>
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        val entry = workerProviders.entries.firstOrNull { it.key.name == workerClassName }
        return entry?.value?.get()?.create(appContext, workerParameters)
    }
}
