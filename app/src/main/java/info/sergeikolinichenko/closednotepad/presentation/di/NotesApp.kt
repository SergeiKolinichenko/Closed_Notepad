package info.sergeikolinichenko.closednotepad.presentation.di

import android.app.Application
import androidx.work.Configuration
import info.sergeikolinichenko.closednotepad.workers.RemovedNoteListWorkerFactory
import javax.inject.Inject

/** Created by Sergei Kolinichenko on 25.11.2022 at 19:04 (GMT+3) **/

class NotesApp: Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: RemovedNoteListWorkerFactory

    val component by lazy {
        DaggerApplicationComponent.factory().create(this)
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder().setWorkerFactory(workerFactory).build()
    }

    override fun onCreate() {
        component.inject(this)
        super.onCreate()
    }

}