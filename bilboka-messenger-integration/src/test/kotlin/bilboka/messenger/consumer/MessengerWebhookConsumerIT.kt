package bilboka.messenger.consumer

import bilboka.messenger.dto.FacebookMessage
import io.mockk.mockkStatic
import io.mockk.verify
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@TestInstance(PER_CLASS)
internal class MessengerWebhookConsumerIT {

    lateinit var webhookConsumer: MessengerWebhookConsumer
    lateinit var testUrl: String
    lateinit var mockBackEnd: MockWebServer

    val pageAccessToken: String = "testPageAccess"

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
        webhookConsumer = MessengerWebhookConsumer(testUrl, pageAccessToken)
    }

    @Test
    fun sendMessage_correctPostCall() {

        // Arrange
        mockBackEnd.enqueue(
            MockResponse()
                .setBody("\"test\" : \"test\"")
                .addHeader("Content-Type", "application/json")
        )

        mockkStatic("khttp.KHttp")

        val recipient = mapOf(Pair("id", "123"))
        val testMessage = "detteerentest"
        val message = mapOf(Pair("text", testMessage))

        val testFBMessage = FacebookMessage(
            timestamp = 1L,
            recipient = recipient,
            message = message
        )

        // Act
        webhookConsumer.sendMessage(testFBMessage)

        // Assert
        verify {
            khttp.post(
                url = eq(testUrl),
                headers = any(),
                data = any()
            )
        }

        val takeRequest = mockBackEnd.takeRequest()

        assertThat(takeRequest.method).isEqualTo("POST")
        assertThat(takeRequest.path).isEqualTo("/")
        assertThat(takeRequest.headers).contains(Pair("access_token", pageAccessToken))
        assertThat(takeRequest.body.readUtf8())
            .contains(testMessage)
            .contains("\"text\":")
    }

}