package info.sergeikolinichenko.closednotepad.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters

/** Created by Sergei Kolinichenko on 04.12.2022 at 17:35 (GMT+3) **/

interface ChildWorkerFactory {

    fun create(
        context: Context,
        workerParameters: WorkerParameters
    ): ListenableWorker
}