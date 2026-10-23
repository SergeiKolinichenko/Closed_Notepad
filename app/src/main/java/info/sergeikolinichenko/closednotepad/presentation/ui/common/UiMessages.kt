package info.sergeikolinichenko.closednotepad.presentation.ui.common

import info.sergeikolinichenko.closednotepad.presentation.di.ApplicationScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

// text is a plurals resource when count is set, otherwise a string resource
data class UiMessage(
    val text: Int,
    val count: Int? = null,
    val undo: (suspend () -> Unit)? = null
)

// Snackbar messages that may outlive the screen that sent them,
// e.g. "Moved to trash" sent by an editor that is being closed
@ApplicationScope
class UiMessages @Inject constructor() {

    private val channel = Channel<UiMessage>(Channel.BUFFERED)
    val messages: Flow<UiMessage> = channel.receiveAsFlow()

    fun show(message: UiMessage) {
        channel.trySend(message)
    }
}
