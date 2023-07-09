package bilboka.messagebot.commands

import bilboka.core.user.UserService
import bilboka.core.vehicle.VehicleService
import bilboka.integration.autosys.dto.Kjoretoydata
import bilboka.messagebot.Conversation
import bilboka.messagebot.commands.common.CarBookCommand

internal class VehicleInfoDekkOgFelg(
    private val vehicleService: VehicleService,
    userService: UserService
) : CarBookCommand(userService) {
    private val matcher = Regex(
        "(autosys-dekkogfelg)\\s+([\\wæøå]+([\\s-]+?[\\wæøå]+)?)",
        RegexOption.IGNORE_CASE
    )

    override fun isMatch(message: String): Boolean {
        return matcher.containsMatchIn(message)
    }

    override fun execute(conversation: Conversation, message: String) {
        val values = matcher.find(message)!!.groupValues
        val vehicleName = values[2]

        vehicleService.getAutosysKjoretoydata(vehicleName).apply {
            replyWithInfo(this, conversation)
        }
    }

    private fun replyWithInfo(
        data: Kjoretoydata,
        conversation: Conversation
    ) {
        conversation.sendReply(
            "\uD83D\uDE97 Dekk- og felgdata fra Autosys for " +
                    "${
                        data.kjoretoyId?.kjennemerke ?: data.kjoretoyId?.understellsnummer ?: "(ukjent)"
                    } \n" +
                    "${
                        data.godkjenning?.tekniskGodkjenning?.tekniskeData?.dekkOgFelg?.akselDekkOgFelgKombinasjon
                            ?.first()?.akselDekkOgFelg?.map {
                                "Første kombinasjon: \n" +
                                        "- Dekkdimensjon: ${it.dekkdimensjon} \n" +
                                        "- Felgdimensjon: ${it.felgdimensjon} \n" +
                                        "- Belastningskode: ${it.belastningskodeDekk} \n"
                            }?.joinToString("\n") ?: "(ukjent)"
                    } \n"
        )
    }
}
