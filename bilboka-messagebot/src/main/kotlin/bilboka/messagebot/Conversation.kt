package bilboka.messagebot

import bilboka.core.user.domain.User
import bilboka.messagebot.commands.common.Undoable
import java.time.Duration
import java.time.Instant

class Conversation(
    var user: User? = null,
    val senderID: String,
    val botMessenger: BotMessenger
) {
    private val duplicateBuster = DuplicateBuster(senderID)

    private var lastUndoable: UndoableEvent<Any>? = null

    fun getSource(): String {
        return botMessenger.sourceID
    }

    fun withWhom(): User {
        return user
            ?: throw DontKnowWithWhomException("Samtalen kjenner ikke til bruker med id $senderID for meldingskilde ${getSource()}")
    }

    fun registerUser(user: User) {
        if (this.user != null) {
            throw IllegalStateException("Kan ikke endre bruker på eksisterende samtale")
        }
        this.user = user
    }

    fun sendReply(message: String) {
        botMessenger.sendMessage(
            message,
            senderID
        )
    }

    fun validate(message: String) {
        duplicateBuster.catchDuplicates(message)
    }

    fun undoLast() {
        lastUndoable?.apply {
            action.undo(item)
        } ?: throw NothingToUndoException("Ingen handling å angre")
        resetUndoable()
    }

    fun <T : Any> setUndoable(action: Undoable<T>, item: T) {
        lastUndoable = UndoableEvent(action, item) as UndoableEvent<Any>
    }

    fun resetUndoable() {
        lastUndoable = null
    }

    internal class DuplicateBuster(private val sender: String) {
        private val timeout = Duration.ofSeconds(10)
        private var last: String? = null
        private var lastTime: Instant = Instant.now().minus(timeout)

        fun catchDuplicates(message: String) {
            if (isDuplicate(message, sender)) {
                throw StopRepeatingYourselfException()
            } else {
                updateLastWith(message, sender)
            }
        }

        private fun isDuplicate(message: String, sender: String) =
            last == identifier(message, sender) && Instant.now().isBefore(lastTime.plus(timeout))

        private fun updateLastWith(message: String, sender: String) {
            lastTime = Instant.now()
            last = identifier(message, sender)
        }

        private fun identifier(message: String, sender: String) = "$sender:$message"
    }
}

data class UndoableEvent<T : Any>(val action: Undoable<T>, val item: T)

class StopRepeatingYourselfException : RuntimeException()
class DontKnowWithWhomException(message: String) : RuntimeException(message)
class NothingToUndoException(message: String) : RuntimeException(message)
