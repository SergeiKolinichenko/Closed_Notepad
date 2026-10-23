package info.sergeikolinichenko.closednotepad.presentation.security

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import info.sergeikolinichenko.closednotepad.presentation.di.ApplicationScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// Remembers which locked notes the user has unlocked. Everything is locked again
// as soon as the app goes to the background.
@ApplicationScope
class NoteLock @Inject constructor() : DefaultLifecycleObserver {

    private val _unlocked = MutableStateFlow<Set<Long>>(emptySet())
    val unlocked: StateFlow<Set<Long>> = _unlocked.asStateFlow()

    private val openNotes = mutableMapOf<Long, Int>()

    fun markUnlocked(ids: Collection<Long>) {
        _unlocked.update { it + ids }
    }

    // Editors register the note they show, so it is not deleted as blank meanwhile
    @Synchronized
    fun noteOpened(id: Long) {
        openNotes[id] = (openNotes[id] ?: 0) + 1
    }

    @Synchronized
    fun noteClosed(id: Long) {
        val count = (openNotes[id] ?: return) - 1
        if (count <= 0) openNotes.remove(id) else openNotes[id] = count
    }

    @Synchronized
    fun openNoteIds(): Set<Long> = openNotes.keys.toSet()

    override fun onStop(owner: LifecycleOwner) {
        _unlocked.value = emptySet()
    }
}
