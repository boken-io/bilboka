package bilboka.messagebot

import bilboka.core.user.domain.User
import bilboka.messagebot.commands.common.ChatCommand
import bilboka.messagebot.commands.common.ChatState
import bilboka.messagebot.commands.common.Undoable
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant

internal class Conversation(
    var user: User? = null,
    val senderID: String,
    val botMessenger: BotMessenger
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val duplicateBuster = DuplicateBuster(senderID)
    private var lastUndoable: UndoableEvent<Any>? = null
    internal var claim: ConversationClaim<ChatCommand>? = null

    fun getSource(): String {
        return botMessenger.sourceID
    }

    fun withWhom(): User {
        return user
            ?: throw DontKnowWithWhomException("Samtalen kjenner ikke til bruker med id $senderID for meldingskilde ${getSource()}")
    }

    fun sendReply(message: String) {
        logger.debug("[meldingslogg] Sender melding '$message'")
        botMessenger.sendMessage(
            message.run { if (lastUndoable?.valid == true) "✅ $this" else this },
            senderID
        )
    }

    fun replyWithOptions(message: String, vararg options: Pair<String, String>) {
        logger.debug("[meldingslogg] Sender melding '$message' med ${options.size} svaralternativer")
        botMessenger.sendOptions(message, options.asList(), senderID)
    }

    fun sendPdf(file: ByteArray, fileName: String) {
        logger.debug("[meldingslogg] Sender pdf $fileName")
        botMessenger.sendPdf(
            file,
            "$fileName.pdf",
            senderID
        )
    }

    fun validate(message: String) {
        duplicateBuster.catchDuplicates(message)
    }

    fun <T : ChatState> claim(by: ChatCommand, state: T) {
        logger.debug("Claimed by: ${by.javaClass.name}")
        this.claim = ConversationClaim(by, state)
    }

    inline fun <reified T : ChatState> withdrawClaim(by: ChatCommand): T? {
        if (claimedBy(by)) {
            logger.debug("Claim withdrawn: ${by.javaClass.name}")
            return claim?.state.also { unclaim() } as T
        }
        return null
    }

    fun claimedBy(by: ChatCommand): Boolean {
        return claim?.claimedBy == by
    }

    fun hasClaim(): Boolean {
        return claim != null
    }

    fun unclaim() {
        this.claim = null
    }

    fun registerUser(user: User) {
        if (this.user != null) {
            throw IllegalStateException("Kan ikke endre bruker på eksisterende samtale")
        }
        this.user = user
    }

    fun undoLast() {
        lastUndoable?.takeIf { it.valid }?.apply {
            action.undo(item)
        } ?: throw NothingToUndoException("Ingen handling å angre")
        resetUndoable()
    }

    fun <T : Any> setUndoable(action: Undoable<T>, item: T) {
        lastUndoable = UndoableEvent(action, item) as UndoableEvent<Any>
    }

    fun resetUndoable() {
        lastUndoable?.takeIf { it.valid }?.apply { valid = false } ?: { lastUndoable = null }
    }

    fun keepUndoable() {
        lastUndoable?.apply { valid = true }
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

internal data class UndoableEvent<T : Any>(val action: Undoable<T>, val item: T) {
    var valid = true
}

internal data class ConversationClaim<T : ChatCommand>(val claimedBy: T, val state: ChatState?)

class StopRepeatingYourselfException : RuntimeException()
class DontKnowWithWhomException(message: String) : RuntimeException(message)
class NothingToUndoException(message: String) : ImpossibleChatActionException(message)
