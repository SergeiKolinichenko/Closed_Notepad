package info.sergeikolinichenko.closednotepad.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import androidx.work.WorkManager
import info.sergeikolinichenko.closednotepad.R
import info.sergeikolinichenko.closednotepad.presentation.di.AppCoroutineScope
import info.sergeikolinichenko.closednotepad.presentation.di.ApplicationComponent
import info.sergeikolinichenko.closednotepad.presentation.di.DaggerApplicationComponent
import info.sergeikolinichenko.closednotepad.presentation.security.NoteLock
import info.sergeikolinichenko.closednotepad.presentation.ui.common.UiMessage
import info.sergeikolinichenko.closednotepad.presentation.ui.common.UiMessages
import info.sergeikolinichenko.closednotepad.repositories.NotesRepository
import info.sergeikolinichenko.closednotepad.security.LockedNotesEncryption
import info.sergeikolinichenko.closednotepad.security.NotesVault
import info.sergeikolinichenko.closednotepad.workers.RemovedNoteListWorker
import info.sergeikolinichenko.closednotepad.workers.RemovedNoteListWorkerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Created by Sergei Kolinichenko on 25.11.2022 at 19:04 (GMT+3) **/

class NotesApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: RemovedNoteListWorkerFactory

    @Inject
    lateinit var noteLock: NoteLock

    @Inject
    lateinit var notesRepository: NotesRepository

    @Inject
    lateinit var notesVault: NotesVault

    @Inject
    lateinit var lockedNotesEncryption: LockedNotesEncryption

    @Inject
    lateinit var uiMessages: UiMessages

    @Inject
    @AppCoroutineScope
    lateinit var appScope: CoroutineScope

    val component: ApplicationComponent by lazy {
        DaggerApplicationComponent.factory().create(this)
    }

    override fun onCreate() {
        super.onCreate()
        component.inject(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(noteLock)
        RemovedNoteListWorker.schedule(WorkManager.getInstance(this))
        appScope.launch {
            // Empty notes can be left behind if the app was killed right after creating one
            notesRepository.deleteBlankNotes(noteLock.openNoteIds())
        }
        appScope.launch {
            // Locked notes written before the password was set are encrypted as soon as the vault
            // is open: right after the password is set, and on any later unlock if some were missed
            notesVault.unlocked.filter { it }.collect {
                runCatching { lockedNotesEncryption.encryptPlainLockedNotes() }
                    .onSuccess { count -> if (count > 0) uiMessages.show(UiMessage(R.plurals.notes_encrypted, count = count)) }
                    .onFailure { error ->
                        Log.e(TAG, "Locked notes were not encrypted", error)
                        uiMessages.show(UiMessage(R.string.encryption_failed))
                    }
            }
        }
    }

    companion object {
        private const val TAG = "NotesApp"
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
