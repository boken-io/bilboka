package bilboka.messenger.consumer

import bilboka.messenger.MessengerProperties
import bilboka.messenger.dto.*
import io.mockk.InternalPlatformDsl.toStr
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@TestInstance(PER_CLASS)
// TODO prop inject?
internal class MessengerProfileAPIConsumerIT {

    private lateinit var profileConsumer: MessengerProfileAPIConsumer
    private lateinit var testUrl: String
    private lateinit var mockBackEnd: MockWebServer

    private val pageAccessToken: String = "testPageAccess"

    @BeforeAll
    fun setUp() {
        mockBackEnd = MockWebServer()
        mockBackEnd.start()
    }

    @AfterAll
    fun tearDown() {
        mockBackEnd.shutdown()
    }

    @BeforeEach
    fun initialize() {
        testUrl = String.format(
            "http://localhost:%s",
            mockBackEnd.port
        )
        val messengerProperties = MessengerProperties()
        messengerProperties.profileUrl = testUrl
        messengerProperties.pageAccessToken = pageAccessToken
        profileConsumer = MessengerProfileAPIConsumer(messengerProperties)
    }

    @Test
    fun sendMessage_correctPostCall() {
        // Arrange
        mockBackEnd.enqueue(
            MockResponse()
                .setBody("\"test\" : \"test\"")
                .addHeader("Content-Type", "application/json")
        )

        val testPayload = "detteerentest"

        val profilConfig = MessengerProfileRequest(
            persistentMenu = listOf(
                PersistentMenu(
                    callToActions = listOf(
                        PersistentMenuItem(
                            title = "En greie",
                            payload = testPayload
                        )
                    )
                )
            ),
            getStarted = GetStarted("Test")
        )

        // Act
        profileConsumer.doProfileUpdate(profilConfig)

        val takeRequest = mockBackEnd.takeRequest()

        assertThat(takeRequest.method).isEqualTo("POST")
        assertThat(takeRequest.requestUrl.toStr()).contains(mockBackEnd.port.toStr())
        assertThat(takeRequest.requestUrl.toStr()).contains(pageAccessToken)
        assertThat(takeRequest.headers["Content-Type"]).contains("application/json")
        assertThat(takeRequest.body.readUtf8())
            .contains("\"payload\":\"$testPayload\"")
            .contains("persistent_menu")
            .contains("call_to_actions")
            .doesNotContain("mid").doesNotContain("seq")
    }
}
