package bilboka.messagebot.commands.common

import bilboka.core.user.UserService

internal abstract class CarBookCommand(
    private val userService: UserService
) : ChatCommand() {
    override fun validUser(regTypeID: String, senderID: String): Boolean {
        return userService.findUserByRegistration(regTypeID, senderID) != null
    }
}
