package bilboka.messagebot

import bilboka.core.vehicle.VehicleNotFoundException
import bilboka.core.vehicle.domain.FuelType
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@ExtendWith(MockKExtension::class)
class LastEntryGetterTest : AbstractMessageBotTest() {

    @Test
    @Disabled // Disse testene er ganske ubrukelige
    fun sendGetLastEntry_repliedWithLastEntry() {
        val time = LocalDateTime.of(LocalDate.of(2020, 1, 1), LocalTime.NOON)
        val vehicle = vehicle(name = "En Testbil", fuelType = FuelType.DIESEL)
        every { book.getLastFuelEntry(any()) } returns fuelEntry(
            vehicle = vehicle, dateTime = time, odometer = 1234, amount = 30.0, costNOK = 100.0
        )
        every { book.maintenanceItems() } returns emptySet()
        every { vehicleService.getVehicle(any()) } returns vehicle

        messagebot.processMessage("Siste testbil", registeredSenderID)

        verify {
            botMessenger.sendMessage(
                message = "Siste tanking av En Testbil: 30 liter for 100 kr (3,33 kr/l) ${time.format()} ved 1234 km",
                registeredSenderID
            )
        }
    }

    @Test
    @Disabled // Disse testene er ganske ubrukelige
    fun sendGetLastEntryWhenNoEntries_repliesSomethingUseful() {
        every { book.getLastFuelEntry(any()) } returns null

        messagebot.processMessage("Siste testbil", registeredSenderID)

        verify {
            botMessenger.sendMessage(
                "Finner ingen tankinger for testbil",
                registeredSenderID
            )
        }
    }

    @Test
    fun sendGetLastEntryWhenCarNotFound_repliesSomethingUseful() {
        every { book.getLastFuelEntry(any()) } throws VehicleNotFoundException("Ops", "bil")

        messagebot.processMessage("Siste testbil", registeredSenderID)

        verify {
            botMessenger.sendMessage(
                any(),
                registeredSenderID
            )
        }
    }

}
