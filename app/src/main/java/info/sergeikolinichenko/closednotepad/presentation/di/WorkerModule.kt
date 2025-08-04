package info.sergeikolinichenko.closednotepad.presentation.di

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import info.sergeikolinichenko.closednotepad.workers.ChildWorkerFactory
import info.sergeikolinichenko.closednotepad.workers.RemovedNoteListWorker

/** Created by Sergei Kolinichenko on 04.12.2022 at 17:36 (GMT+3) **/

@Module
interface WorkerModule {

    @Binds
    @IntoMap
    @WorkerKey(RemovedNoteListWorker::class)
    fun bindRemovedNoteListWorker(worker: RemovedNoteListWorker.Factory): ChildWorkerFactory
}