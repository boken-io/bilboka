/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package bilboka.messagebot

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MessageBotTest {
    @Test
    fun testHandleMessage() {
        val messagebot = MessageBot()
        assertThat(messagebot.processMessage("Hei")).isNotBlank
    }
}
