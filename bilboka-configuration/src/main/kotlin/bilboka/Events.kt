package bilboka

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class Events : ApplicationListener<ApplicationReadyEvent> {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @Autowired(required = false)
    var serverProperties: ServerProperties? = null

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        logger.info("Application ready!")
        serverProperties?.port?.let { logger.info("Running on port: $it") } ?: logger.warn("Missing serverProperties!")
    }
}
