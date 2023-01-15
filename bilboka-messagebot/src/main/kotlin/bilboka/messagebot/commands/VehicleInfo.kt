package bilboka.messagebot.commands

import bilboka.core.user.UserService
import bilboka.core.vehicle.VehicleService
import bilboka.core.vehicle.domain.Vehicle
import bilboka.messagebot.BotMessenger

class VehicleInfo(
    private val botMessenger: BotMessenger,
    private val vehicleService: VehicleService,
    userService: UserService
) : CarBookCommand(userService) {
    private val matcher = Regex(
        "(inf|info|kjøretøyinfo)\\s+(\\w+([\\s-]+?\\w+)?)",
        RegexOption.IGNORE_CASE
    )

    override fun isMatch(message: String): Boolean {
        return matcher.containsMatchIn(message)
    }

    override fun execute(senderID: String, message: String) {
        val values = matcher.find(message)!!.groupValues
        val vehicleName = values[2]

        vehicleService.findVehicle(vehicleName).apply {
            replyWithInfo(this, senderID)
        }
    }

    private fun replyWithInfo(
        vehicle: Vehicle,
        senderID: String
    ) {
        botMessenger.sendMessage(
            "Bil-navn: ${vehicle.name} \n" +
                    "Alternative navn: ${vehicle.nicknames.joinToString(", ")} \n" +
                    "Registreringsnummer: ${vehicle.tegnkombinasjonNormalisert ?: "(ikke registrert)"} \n" +
                    "Distansemåleenhet: ${vehicle.odometerUnit} \n" +
                    "Drivstofftype: ${vehicle.fuelType} \n" +
                    "Antall oppføringer: ${vehicle.bookEntries.count()} \n" +
                    "Sist registrert km-stand: ${vehicle.lastEntry()?.odometer ?: "?"}",
            senderID
        )
    }

    override fun resetState() {

    }
}
