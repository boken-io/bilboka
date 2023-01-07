package bilboka.messagebot

import bilboka.messagebot.commands.DEFAULT_HELP_MESSAGE
import io.mockk.confirmVerified
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class MessageBotTest : AbstractMessageBotTest() {

    @Test
    fun sendHei_returnsHei() {
        messagebot.processMessage("Hei", senderID)

        verify { botMessenger.sendMessage("Hei", senderID) }
        confirmVerified(botMessenger)
    }

    @Test
    fun sendSkjer_returnsIkkenospes() {
        messagebot.processMessage("Skjer?", senderID)

        verify { botMessenger.sendMessage("Ikke noe spes. Der?", senderID) }
        confirmVerified(botMessenger)
    }

    @Test
    fun sendSkjerThenSomethingElse_returnsCool() {
        messagebot.processMessage("Skjer?", senderID)
        messagebot.processMessage("Holder på med noe greier", senderID)

        verifyOrder {
            botMessenger.sendMessage("Ikke noe spes. Der?", senderID)
            botMessenger.sendMessage("Cool", senderID)
        }
        confirmVerified(botMessenger)
    }

    @Test
    fun sendSkjerThenSomethingMatcinghOtherRule_returnsOtherRuleResponse() {
        messagebot.processMessage("Skjer?", senderID)
        messagebot.processMessage("Stuff skjer", senderID)
        messagebot.processMessage("Help", senderID)

        verifyOrder {
            botMessenger.sendMessage("Ikke noe spes. Der?", senderID)
            botMessenger.sendMessage("Cool", senderID)
            botMessenger.sendMessage(DEFAULT_HELP_MESSAGE, senderID)
        }
        confirmVerified(botMessenger)
    }

    @Test
    fun sendSomethingStrange_returnsDefaultMessage() {
        messagebot.processMessage("Her kommer en rar melding", senderID)

        verify { botMessenger.sendMessage(FALLBACK_MESSAGE, senderID) }
        confirmVerified(botMessenger)
    }
}
