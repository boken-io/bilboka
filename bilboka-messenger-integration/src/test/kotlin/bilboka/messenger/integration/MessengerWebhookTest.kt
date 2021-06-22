package bilboka.messenger.integration

import bilboka.messenger.dto.FacebookEntry
import bilboka.messenger.dto.FacebookMessage
import bilboka.messenger.dto.FacebookMessaging
import bilboka.messenger.dto.MessengerWebhookRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.bind.Bindable.listOf
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Collections.emptyList
import java.util.Collections.emptyMap

@RunWith(SpringRunner::class)
@WebMvcTest(MockMvc::class)
internal class MessengerWebhookTest {

    @Autowired
    lateinit var mvc: MockMvc

    @BeforeEach
    fun setUp() {
    }

    @Nested
    inner class GetWebhookTests {

        @Test
        fun getRequestWithoutParams_returnsBadRequest() {
            mvc.perform(
                get("/webhook")
                    .contentType(MediaType.TEXT_HTML)
            )
                .andExpect(status().isBadRequest())
        }

        @Test
        fun getRequestMissingParams_returnsBadRequest() {
            mvc.perform(
                get("/webhook?hub.verify_token=vdfgsnmrfeiudi59fblablajvbrmeivncmq231v&hub.mode=subscribe")
                    .contentType(MediaType.TEXT_HTML)
            )
                .andExpect(status().isBadRequest())
        }

        @Test
        fun validGetRequest_returnsChallengeAccepted() {
            mvc.perform(
                get("/webhook?hub.verify_token=vdfgsnmrfeiudi59fblablajvbrmeivncmq231v&hub.challenge=CHALLENGE_ACCEPTED&hub.mode=subscribe")
                    .contentType(MediaType.TEXT_HTML)
            )
                .andExpect(content().string("CHALLENGE_ACCEPTED"))
        }

        @Test
        fun validGetRequest_returnsSomeOtherChallenge() {
            mvc.perform(
                get("/webhook?hub.verify_token=vdfgsnmrfeiudi59fblablajvbrmeivncmq231v&hub.challenge=rullekake&hub.mode=subscribe")
                    .contentType(MediaType.TEXT_HTML)
            )
                .andExpect(content().string("rullekake"))
        }

        @Test
        fun invalidToken_returns403() {
            mvc.perform(
                get("/webhook?hub.verify_token=blah&hub.challenge=CHALLENGE_ACCEPTED&hub.mode=subscribe")
                    .contentType(MediaType.TEXT_HTML)
            )
                .andExpect(status().isForbidden())
        }

        @Test
        fun invalidMode_returns403() {
            mvc.perform(
                get("/webhook?hub.verify_token=vdfgsnmrfeiudi59fblablajvbrmeivncmq231v&hub.challenge=CHALLENGE_ACCEPTED&hub.mode=unsubscribe")
                    .contentType(MediaType.TEXT_HTML)
            )
                .andExpect(status().isForbidden())
        }
    }

    @Nested
    inner class PostWebhookTests {
        @Test
        fun postRequestEmptyList_returnsOk() {
            mvc.perform(
                post("/webhook")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        asJsonString(
                            MessengerWebhookRequest(
                                requestObject = "Blah",
                                entry = emptyList<FacebookEntry>()
                            )
                        )
                    )
            )
                .andExpect(status().isOk())
        }

        @Test
        fun postRequestSomeList_returnsOk() {
            mvc.perform(
                post("/webhook")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        asJsonString(
                            MessengerWebhookRequest(
                                requestObject = "Blah", entry = listOf<FacebookEntry>(
                                    FacebookEntry(id = "123", time = 123L, messaging = emptyList<FacebookMessaging>())
                                )
                            )
                        )
                    )
            )
                .andExpect(status().isOk())
        }

        @Test
        fun postRequestSomeListWithMessaging_returnsOk() {
            mvc.perform(
                post("/webhook")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        asJsonString(
                            MessengerWebhookRequest(
                                requestObject = "Blah", entry = listOf<FacebookEntry>(
                                    FacebookEntry(
                                        "123", 123L, listOf<FacebookMessaging>(
                                            FacebookMessaging(
                                                1234L,
                                                emptyMap<String, String>(),
                                                emptyMap<String, String>(),
                                                FacebookMessage(
                                                    1234L,
                                                    emptyMap<String, String>(),
                                                    emptyMap<String, String>(),
                                                    emptyMap<String, String>()
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
            )
                .andExpect(status().isOk())
        }

        @Test
        fun postRequestWithoutBody_returnsBadRequest() {
            mvc.perform(
                post("/webhook")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                .andExpect(status().isBadRequest())
        }

    }

    fun asJsonString(obj: Any): String {
        return try {
            ObjectMapper().writeValueAsString(obj)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
