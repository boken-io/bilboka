package bilboka.messagebot;

import org.junit.jupiter.api.Test

class MessageBotMaintenanceIT : AbstractMessageBotIT() {

    @Test
    fun canGetMaintenanceOptions() {
        processMessagaAndAssertReply(
            message = "bytte bremseklosser xc70 45677",
            reply = { it.contains("Legge til 'bremseklosser' som et vedlikeholdspunkt?") },
        )
        processMessagaAndAssertReply(
            message = "ja",
            reply = { it.contains("kommentar") },
        )
        processMessagaAndAssertReply(
            message = "nei",
            reply = { it.contains("Registrert") },
        )
        processMessagaAndAssertReply(
            message = "vedlikehold",
            reply = { it.contains("BREMSEKLOSSER") },
        )
    }

    @Test
    fun canGetLastMaintenance() {
        processMessagaAndAssertReply(
            message = "bytte bremseskiver xc70 45677",
            reply = { it.contains("Legge til 'bremseskiver' som et vedlikeholdspunkt?") },
        )
        processMessagaAndAssertReply(
            message = "ja",
            reply = { it.contains("kommentar") },
        )
        processMessagaAndAssertReply(
            message = "nei",
            reply = { it.contains("Registrert") },
        )
        processMessagaAndAssertReply(
            message = "Siste bremseskiver xc70",
            reply = { it.contains("45677") },
        )
    }

    @Test
    fun canDoMaintenanceWithMultipleWords() {
        processMessagaAndAssertReply(
            message = "bytte bremse-skiver foran xc70 45684",
            reply = { it.contains("Legge til 'bremse-skiver foran' som et vedlikeholdspunkt?") },
        )
        processMessagaAndAssertReply(
            message = "ja",
            reply = { it.contains("kommentar") },
        )
        processMessagaAndAssertReply(
            message = "nei",
            reply = { it.contains("Registrert BREMSE-SKIVER_FORAN") },
        )
        processMessagaAndAssertReply(
            message = "Siste bremse-skiver foran xc70",
            reply = { it.contains("45684") },
        )
    }

    @Test
    fun repliesUnknownVehicle() {
        processMessagaAndAssertReply(
            message = "bytte bremseskiver bak xc70 45684",
            reply = { it.contains("Legge til 'bremseskiver bak' som et vedlikeholdspunkt?") },
        )
        processMessagaAndAssertReply(
            message = "ja",
            reply = { it.contains("kommentar") },
        )
        processMessagaAndAssertReply(
            message = "nei",
            reply = { it.contains("Registrert") },
        )
        processMessagaAndAssertReply(
            message = "Siste bremseskiver bak blah",
            reply = { it.contains("Ukjent bil") },
        )
    }

    @Test
    fun canRegisterExistingMaintenance() {
        processMessagaAndAssertReply(
            message = "bytte blinklysvæske xc70 45677",
            reply = { it.contains("Legge til 'blinklysvæske' som et vedlikeholdspunkt?") },
        )
        processMessagaAndAssertReply(
            message = "ja",
            reply = { it.contains("kommentar") },
        )
        processMessagaAndAssertReply(
            message = "nei",
            reply = { it.contains("Registrert") },
        )
        processMessagaAndAssertReply(
            message = "bytte blinklysvæske xc70 45800",
            reply = { it.contains("kommentar") },
        )
        processMessagaAndAssertReply(
            message = "nei",
            reply = { it.contains("Registrert BLINKLYSVESKE ved 45800") },
        )
    }

    @Test
    fun canRegisterExistingMaintenanceInDifferentOrder() {
        processMessagaAndAssertReply(
            message = "bytte vindusviskere xc70 45677",
            reply = { it.contains("Legge til 'vindusviskere' som et vedlikeholdspunkt?") },
        )
        processMessagaAndAssertReply(
            message = "ja",
            reply = { it.contains("kommentar") },
        )
        processMessagaAndAssertReply(
            message = "nei",
            reply = { it.contains("Registrert") },
        )
        processMessagaAndAssertReply(
            message = "bytte vindusviskere 45800 xc70",
            reply = { it.contains("kommentar") },
        )
        processMessagaAndAssertReply(
            message = "nei",
            reply = { it.contains("Registrert VINDUSVISKERE ved 45800") },
        )
    }

    @Test
    fun repliesMissingCar() {
        processMessagaAndAssertReply(
            message = "bytte vindusviskere 45677",
            reply = { it.contains("Hvilken bil? \uD83D\uDE97") },
        )
    }

    @Test
    fun repliesMissingOdo() {
        processMessagaAndAssertReply(
            message = "bytte batteri xc70",
            reply = { it.contains("Kilometerstand? \uD83D\uDD22") },
        )
        processMessagaAndAssertReply(
            message = "23456",
            reply = { it.contains("Hva slags vedlikehold?") },
        )
        processMessagaAndAssertReply(
            message = "batteri",
            reply = { it.contains("Legge til 'batteri' som et vedlikeholdspunkt?") },
        )
        processMessagaAndAssertReply(
            message = "ja",
            reply = { it.contains("kommentar") },
        )
        processMessagaAndAssertReply(
            message = "nei",
            reply = { it.contains("Registrert BATTERI ved 23456") },
        )
    }

    @Test
    fun repliesMissingVehicle() {
        processMessagaAndAssertReply(
            message = "bytte girkasse 23457",
            reply = { it.contains("Hvilken bil") },
        )
        processMessagaAndAssertReply(
            message = "xc 70",
            reply = { it.contains("Hva slags vedlikehold?") },
        )
        processMessagaAndAssertReply(
            message = "girkasse",
            reply = { it.contains("Legge til 'girkasse' som et vedlikeholdspunkt?") },
        )
        processMessagaAndAssertReply(
            message = "ja",
            reply = { it.contains("kommentar") },
        )
        processMessagaAndAssertReply(
            message = "nei",
            reply = { it.contains("Registrert GIRKASSE ved 23457") },
        )
    }

    @Test
    fun canRegisterExistingMaintenancePartWise() {
        processMessagaAndAssertReply(
            message = "bytte vindu xc70 45677",
            reply = { it.contains("Legge til 'vindu' som et vedlikeholdspunkt?") },
        )
        processMessagaAndAssertReply(
            message = "ja",
            reply = { it.contains("kommentar") },
        )
        processMessagaAndAssertReply(
            message = "nei",
            reply = { it.contains("Registrert") },
        )
        processMessagaAndAssertReply(
            message = "bytte vindu xc70",
            reply = { it.contains("Kilometerstand? \uD83D\uDD22") },
        )
        processMessagaAndAssertReply(
            message = "45697",
            reply = { it.contains("kommentar") },
        )
        processMessagaAndAssertReply(
            message = "nei",
            reply = { it.contains("Registrert VINDU ved 45697") },
        )
        processMessagaAndAssertReply(
            message = "bytte vindu 45777",
            reply = { it.contains("Hvilken bil") },
        )
        processMessagaAndAssertReply(
            message = "xc 70",
            reply = { it.contains("kommentar") },
        )
        processMessagaAndAssertReply(
            message = "nei",
            reply = { it.contains("Registrert VINDU ved 45777") },
        )
    }

    @Test
    fun canRegisterMaintenanceFromStartKeyword() {
        processMessagaAndAssertReply(
            message = "regmaint",
            reply = { it.contains("Hvilken bil") },
        )
        processMessagaAndAssertReply(
            message = "xc 70",
            reply = { it.contains("Kilometerstand? \uD83D\uDD22") },
        )
        processMessagaAndAssertReply(
            message = "45697",
            reply = { it.contains("Hva slags vedlikehold?") },
        )
        processMessagaAndAssertReply(
            message = "overledning",
            reply = { it.contains("Legge til 'overledning' som et vedlikeholdspunkt?") },
        )
        processMessagaAndAssertReply(
            message = "ja",
            reply = { it.contains("kommentar") },
        )
        processMessagaAndAssertReply(
            message = "nei",
            reply = { it.contains("Registrert OVERLEDNING ved 45697") },
        )
    }

    @Test
    fun canHaveUnknownOdometer() {
        processMessagaAndAssertReply(
            message = "bytte ratt xc70",
            reply = { it.contains("Kilometerstand? \uD83D\uDD22") },
        )
        processMessagaAndAssertReply(
            message = "ukjent",
            reply = { it.contains("Hva slags vedlikehold?") },
        )
        processMessagaAndAssertReply(
            message = "ratt",
            reply = { it.contains("Legge til 'ratt' som et vedlikeholdspunkt?") },
        )
        processMessagaAndAssertReply(
            message = "ja",
            reply = { it.contains("kommentar") },
        )
        processMessagaAndAssertReply(
            message = "nei",
            reply = { it.contains("Registrert RATT") },
        )
    }

    @Test
    fun addsAsCommentIfNotAsMaintItem() {
        processMessagaAndAssertReply(
            message = "bytte underledning xc70 45696",
            reply = { it.contains("Legge til 'underledning' som et vedlikeholdspunkt?") },
        )
        processMessagaAndAssertReply(
            message = "nei",
            reply = { it.contains("Lagt til kommentar") and it.contains("underledning") },
        )
    }

    @Test
    fun maintainanceItemOrCommentCanContainMatchForOtherChatRule() {
        processMessagaAndAssertReply(
            message = "vedlikehold service dieselvarmer xc70 45697",
            reply = { it.contains("Legge til 'service dieselvarmer' som et vedlikeholdspunkt?") },
        )
        processMessagaAndAssertReply(
            message = "nei",
            reply = { it.contains("Lagt til kommentar") and it.contains("dieselvarmer") },
        )
    }

    @Test
    fun maintainanceItemOrCommentCanContainMatchForOtherChatRule_1() {
        processMessagaAndAssertReply(
            message = "vedlikehold xc 70",
            reply = { it.contains("Kilometerstand? \uD83D\uDD22") },
        )
        processMessagaAndAssertReply(
            message = "45697",
            reply = { it.contains("Hva slags vedlikehold?") },
        )
        processMessagaAndAssertReply(
            message = "service dieselvarmer",
            reply = { it.contains("Legge til 'service dieselvarmer' som et vedlikeholdspunkt?") },
        )
        processMessagaAndAssertReply(
            message = "nei",
            reply = { it.contains("Lagt til kommentar") and it.contains("dieselvarmer") },
        )
    }

    @Test
    fun canNotHaveUnknownCar() {
        processMessagaAndAssertReply(
            message = "bytte ratt 876534",
            reply = { it.contains("Hvilken bil") },
        )
        processMessagaAndAssertReply(
            message = "ukjent",
            reply = { it.contains("Kjenner ikke til bil") },
        )
    }

    @Test
    fun missingMaintItem() {
        processMessagaAndAssertReply(
            message = "bytte xc70 45677",
            reply = { it.contains("Dette gir ikke mening") },
        )
    }

}
