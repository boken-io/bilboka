package bilboka.messagebot;

import org.junit.jupiter.api.Test

class MessageBotTripsIT : AbstractMessageBotIT() {

    @Test
    fun canStartTrip() {
        processMessagaAndAssertReply(
            message = "start tur xc70 45677",
            reply = { it.contains("Navn på turen?") },
        )
        processMessagaAndAssertReply(
            message = "Gøy tur",
            reply = { it.contains("Tur 'Gøy tur' startet for xc 70 ved 45677") },
        )
    }

    @Test
    fun canEnterStuffPartly() {
        processMessagaAndAssertReply(
            message = "start tur",
            reply = { it.contains("Hvilken bil?") },
        )
        processMessagaAndAssertReply(
            message = "xc 70",
            reply = { it.contains("Kilometerstand?") },
        )
        processMessagaAndAssertReply(
            message = "45677",
            reply = { it.contains("Navn på turen") },
        )
        processMessagaAndAssertReply(
            message = "En annen tur",
            reply = { it.contains("Tur 'En annen tur' startet for xc 70 ved 45677") },
        )
    }

}
