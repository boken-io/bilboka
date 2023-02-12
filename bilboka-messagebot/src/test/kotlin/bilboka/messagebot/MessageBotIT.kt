package bilboka.messagebot;

import org.junit.jupiter.api.Test

class MessageBotIT : AbstractMessageBotIT() {

    @Test
    fun sendAddFuelRequest() {
        processMessagaAndAssertReply(
            message = "Drivstoff en testbil 34567 30l 300kr",
            reply = "⛽ Registrert tanking av en testbil ved 34567 km: 30 liter for 300 kr, 10 kr/l"
        )
    }

    @Test
    fun canFuelWithNorwegianLetters() {
        processMessagaAndAssertReply(
            message = "Drivstoff blå testbil 34567 30l 300kr",
            reply = "⛽ Registrert tanking av blå testbil ved 34567 km: 30 liter for 300 kr, 10 kr/l"
        )
    }

    @Test
    fun canInfoWithNorwegianLetters() {
        processMessagaAndAssertReply(
            message = "Info blå testbil",
            reply = { it.contains("Bil-navn: blå testbil") },
        )
    }

    @Test
    fun sendAddFuelRequestDifferentOrder() {
        processMessagaAndAssertReply(
            message = "Drivstoff en testbil 30l 300kr 34567",
            reply = "⛽ Registrert tanking av en testbil ved 34567 km: 30 liter for 300 kr, 10 kr/l"
        )
    }

    @Test
    fun sendAddFuelRequestOneMissingValue() {
        processMessagaAndAssertReply(
            message = "Drivstoff en testbil 30l 300kr",
            reply = "Kilometerstand? 🔢"
        )
        processMessagaAndAssertReply(
            message = "34567",
            reply = "⛽ Registrert tanking av en testbil ved 34567 km: 30 liter for 300 kr, 10 kr/l"
        )
    }

    @Test
    fun sendAddFuelRequestAnotherMissingValue() {
        processMessagaAndAssertReply(
            message = "Drivstoff en testbil 30l 34567km",
            reply = "Kroner? \uD83D\uDCB8"
        )
        processMessagaAndAssertReply(
            message = "300",
            reply = "⛽ Registrert tanking av en testbil ved 34567 km: 30 liter for 300 kr, 10 kr/l"
        )
    }

    @Test
    fun sendAddFuelRequestDifferentCase() {
        processMessagaAndAssertReply(
            message = "fylt en testbil 5555 km 30.2 L 302.0 Kr",
            reply = "⛽ Registrert tanking av en testbil ved 5555 km: 30,2 liter for 302 kr, 10 kr/l"
        )
    }

    @Test
    fun sendAddFuelRequestDifferentCaseWithComma() {
        processMessagaAndAssertReply(
            message = "Hei drivstoff XC 70 1234 km 30,44 l 608,80 kr.. :D",
            reply = "⛽ Registrert tanking av xc 70 ved 1234 km: 30,44 liter for 608,8 kr, 20 kr/l"
        )
    }

    @Test
    fun sendAddFuelRequestNickname() {
        processMessagaAndAssertReply(
            message = "Hei drivstoff crosser 1235 km 30.44 l 608.80 kr",
            reply = "⛽ Registrert tanking av xc 70 ved 1235 km: 30,44 liter for 608,8 kr, 20 kr/l"
        )
    }

    @Test
    fun sendAddFuelRequestUnknownCar() {
        processMessagaAndAssertReply(
            message = "Drivstoff tullebil 34567 30l 300kr",
            reply = { it.contains("Kjenner ikke til bil tullebil") }
        )
    }

    @Test
    fun canSayHei() {
        processMessagaAndAssertReply(
            message = "Hei",
            reply = "Hei"
        )
    }

    @Test
    fun sendAddFuelRequestInvalidUser_pretendsToNotUnderstand() {
        processMessagaAndAssertReply(
            message = "Drivstoff en testbil 34567 30l 300kr",
            reply = FALLBACK_MESSAGE,
            sender = "5678"
        )
    }

    @Test
    fun sendGetLastFueling() {
        processMessagaAndAssertReply(
            message = "drivstoff XC 70 1234 km 30,44 l 608,80 kr",
            reply = { it.contains("Registrert tanking") }
        )
        processMessagaAndAssertReply(
            message = "Siste xc70",
            reply = { it.contains("Siste tanking av xc 70: 30,44 liter for 608,8 kr (20 kr/l)") }
        )
    }

    @Test
    fun sendGetLastFuelingInvalidUser_pretendsToNotUnderstand() {
        processMessagaAndAssertReply(
            message = "Siste xc70",
            reply = FALLBACK_MESSAGE,
            sender = "5678"
        )
    }

    @Test
    fun sendRegisterRequestRegisteredUser_saysAlreadyRegisteredAndIsReadyForOtherStuff() {
        processMessagaAndAssertReply(
            message = "registrer",
            reply = { it.contains("Du er allerede registrert") },
        )
        processMessagaAndAssertReply(
            message = "hei",
            reply = "Hei"
        )
    }

    @Test
    fun sendRegisterRequestUnregisteredUser_canRegister() {
        processMessagaAndAssertReply(
            message = "registrer",
            reply = { it.contains("Klar for registrering! Skriv din hemmelige kode") },
            sender = "3333"
        )
        processMessagaAndAssertReply(
            message = keyForNewUser,
            reply = { it.contains("Du er registrert!") },
            sender = "3333"
        )
        processMessagaAndAssertReply(
            message = "hei",
            reply = "Hei",
            sender = "3333"
        )
    }

    @Test
    fun usersCanRunRegisteringIndependently() {
        processMessagaAndAssertReply(
            message = "registrer",
            sender = "238845",
            reply = { it.contains("Klar for registrering! Skriv din hemmelige kode") }
        )
        processMessagaAndAssertReply(
            message = "registrer",
            sender = "838845",
            reply = { it.contains("Klar for registrering! Skriv din hemmelige kode") }
        )
        processMessagaAndAssertReply(
            message = "hei",
            reply = "Hei"
        )
    }

    @Test
    fun brukerinfo() {
        processMessagaAndAssertReply(
            message = "brukerinfo",
            reply = "Du er registrert! \n" +
                    "Brukernavn: tester_user"
        )
    }

    @Test
    fun canUndoEvenOnSecondTry() {
        processMessagaAndAssertReply(
            message = "Drivstoff en testbil 35589 30l 300kr",
            reply = { it.contains("Registrert tanking av en testbil ved 35589 km: 30 liter for 300 kr, 10 kr/l") }
        )
        processMessagaAndAssertReply(
            message = "Drivstoff en testbil 35592 20l 200kr",
            reply = { it.contains("Registrert tanking av en testbil ved 35592 km: 20 liter for 200 kr") }
        )
        processMessagaAndAssertReply(
            message = "Anfgre",
            reply = FALLBACK_MESSAGE
        )
        processMessagaAndAssertReply(
            message = "Angre",
            reply = "Angret \uD83D\uDEAE"
        )
        processMessagaAndAssertReply(
            message = "Siste en testbil",
            reply = { it.contains("Siste tanking av en testbil: 30 liter for 300 kr") }
        )
    }

    @Test
    fun canUndoOnlyOnce() {
        processMessagaAndAssertReply(
            message = "Drivstoff en testbil 36590 30l 300kr",
            reply = { it.contains("Registrert tanking av en testbil ved 36590 km: 30 liter for 300 kr, 10 kr/l") }
        )
        processMessagaAndAssertReply(
            message = "Drivstoff en testbil 36592 20l 200kr",
            reply = { it.contains("Registrert tanking av en testbil ved 36592 km: 20 liter for 200 kr") }
        )
        processMessagaAndAssertReply(
            message = "Angre",
            reply = "Angret \uD83D\uDEAE"
        )
        processMessagaAndAssertReply(
            message = "tull",
            reply = FALLBACK_MESSAGE
        )
        processMessagaAndAssertReply(
            message = "Angre",
            reply = { it.contains("Ingen handling å angre") }
        )
    }

    @Test
    fun canNotUndoAfterAnotherMessage() {
        processMessagaAndAssertReply(
            message = "Drivstoff en testbil 37589 30l 300kr",
            reply = { it.contains("Registrert tanking av en testbil ved 37589 km: 30 liter for 300 kr, 10 kr/l") }
        )
        processMessagaAndAssertReply(
            message = "Drivstoff en testbil 37592 20l 200kr",
            reply = { it.contains("Registrert tanking av en testbil ved 37592 km: 20 liter for 200 kr") }
        )
        processMessagaAndAssertReply(
            message = "Siste en testbil",
            reply = { it.contains("Siste tanking av en testbil: 20 liter for 200 kr (10 kr/l)") }
        )
        processMessagaAndAssertReply(
            message = "Hei",
            reply = "Hei"
        )
        processMessagaAndAssertReply(
            message = "Angre",
            reply = { it.contains("Ingen handling å angre") }
        )
        processMessagaAndAssertReply(
            message = "Siste en testbil",
            reply = { it.contains("Siste tanking av en testbil: 20 liter for 200 kr (10 kr/l)") }
        )
    }

    @Test
    fun stepWiseFuelAdding() {
        processMessagaAndAssertReply(
            message = "Drivstoff en testbil 37589 30l 300kr",
            reply = { it.contains("Registrert tanking av en testbil ved 37589 km: 30 liter for 300 kr, 10 kr/l") }
        )
        processMessagaAndAssertReply(
            message = "Drivstoff",
            reply = "Hvilken bil? \uD83D\uDE97"
        )
        processMessagaAndAssertReply(
            message = "XC 70",
            reply = "Kilometerstand? 🔢"
        )
        processMessagaAndAssertReply(
            message = "234567",
            reply = "Antall liter?"
        )
        processMessagaAndAssertReply(
            message = "45,6",
            reply = "Kroner? \uD83D\uDCB8"
        )
        processMessagaAndAssertReply(
            message = "600.5",
            reply = { it.contains("Registrert tanking av xc 70 ved 234567 km: 45,6 liter for 600,5 kr, 13,17 kr/l") }
        )
    }

    @Test
    fun stepWiseFuelAdding_askForNextDataWhenVehicleProvided() {
        processMessagaAndAssertReply(
            message = "Drivstoff XC 70",
            reply = "Kilometerstand? 🔢"
        )
        processMessagaAndAssertReply(
            message = "234567",
            reply = "Antall liter?"
        )
    }

    @Test
    fun stepWiseFuelAdding_canHaveUnknowns() {
        processMessagaAndAssertReply(
            message = "Drivstoff XC 70",
            reply = "Kilometerstand? 🔢"
        )
        processMessagaAndAssertReply(
            message = "ukjent",
            reply = "Antall liter?"
        )
        processMessagaAndAssertReply(
            message = "45,6",
            reply = "Kroner? \uD83D\uDCB8"
        )
        processMessagaAndAssertReply(
            message = "?",
            reply = "Pris per liter?"
        )
        processMessagaAndAssertReply(
            message = "dunno",
            reply = { it.contains("Registrert tanking av xc 70 ved <ukjent> km: 45,6 liter for <ukjent> kr, <ukjent> kr/l") }
        )
    }

    @Test
    fun stepWiseFuelAdding_canUsePricePerLiterToFindPrice() {
        processMessagaAndAssertReply(
            message = "Drivstoff XC 70 234567",
            reply = "Antall liter?"
        )
        processMessagaAndAssertReply(
            message = "45,6",
            reply = "Kroner? \uD83D\uDCB8"
        )
        processMessagaAndAssertReply(
            message = "vet ikke",
            reply = "Pris per liter?"
        )
        processMessagaAndAssertReply(
            message = "16.7",
            reply = { it.contains("Registrert tanking av xc 70 ved 234567 km: 45,6 liter for 761,52 kr, 16,7 kr/l") }
        )
    }

    @Test
    fun stepWiseFuelAdding_canUsePricePerLiterToFindAmountWhileAskingForMissingOdometer() {
        processMessagaAndAssertReply(
            message = "Drivstoff XC 70 761,52 kr",
            reply = "Kilometerstand? \uD83D\uDD22"
        )
        processMessagaAndAssertReply(
            message = "234567",
            reply = "Antall liter?"
        )
        processMessagaAndAssertReply(
            message = "vet ikke",
            reply = "Pris per liter?"
        )
        processMessagaAndAssertReply(
            message = "16.7",
            reply = { it.contains("Registrert tanking av xc 70 ved 234567 km: 45,6 liter for 761,52 kr, 16,7 kr/l") }
        )
    }

    @Test
    fun canUsePricePerAmountInOneGo() {
        processMessagaAndAssertReply(
            message = "Drivstoff en testbil 37589 30l 10 kr/l",
            reply = { it.contains("Registrert tanking av en testbil ved 37589 km: 30 liter for 300 kr, 10 kr/l") }
        )
    }

    @Test
    fun stats() {
        processMessagaAndAssertReply(
            message = "stats",
            reply = { it.contains("Siste registrerte drivstoff-priser") }
        )
        processMessagaAndAssertReply(
            message = "Drivstoff en testbil 37589 30l 10 kr/l",
            reply = { it.contains("Registrert tanking") }
        )
        processMessagaAndAssertReply(
            message = "prisstatistikk",
            reply = { it.contains("10 kr/l") }
        )
    }
}
