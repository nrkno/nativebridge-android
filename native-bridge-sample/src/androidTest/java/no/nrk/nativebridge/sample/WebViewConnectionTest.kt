package no.nrk.nativebridge.sample;

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nrk.nativebridge.*
import no.nrk.nativebridge.sample.topicdata.TestTopicData
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WebViewConnectionTest {

    private val mapper = ObjectMapper().registerModule(KotlinModule())!!

    /**
     * Tests that sending valid data from webview to native works as expected
     */
    @Test
    fun testSendSuccessful(){
        val expectedJavascript = getJavaScript("testTopic", TestTopicData.Out("someText"))

        val connection = WebViewConnection(mapper, object: JavascriptExecutor{
            override fun executeJavascript(javascript: String) {
                assertEquals(expectedJavascript, javascript)
            }
        })

        connection.send("testTopic", TestTopicData.Out("someText"))
    }

    /**
     * Tests that receiving valid data from webview works as expected
     */
    @Test
    fun testReceiveSuccessful(){
        val connection = WebViewConnection(mapper, object: JavascriptExecutor{
            override fun executeJavascript(javascript: String) {
                /** no-op */
            }
        })

        val javascript = """{"topic":"testTopic","data":{"text":"someText"}}""".trimIndent()

        connection.addHandler("testTopic") { data: TestTopicData.In, _ ->
            assertEquals("someText", data.text)
        }

        connection.receive(javascript)
    }

    /**
     * Tests that error is returned when payload doesn't contain "topic" object
     */
    @Test
    fun testMissingFieldTopicShouldReturnError(){
        val javascript = """{"data":{"key":"value"}}""".trimIndent()

        val connection = WebViewConnection(mapper, object: JavascriptExecutor{
            override fun executeJavascript(javascript: String) {
                assertTrue(javascript.contains(WebViewConnectionError.MissingFieldTopic().message))
            }
        })

        connection.receive(javascript)
    }


    /**
     * Tests that error is returned when payload doesn't contain "data" object
     */
    @Test
    fun testMissingFieldDataShouldReturnError(){
        val javascript = """{"topic":"testTopic"}""".trimIndent()

        val connection = WebViewConnection(mapper, object: JavascriptExecutor{
            override fun executeJavascript(javascript: String) {
                assertTrue(javascript.contains(WebViewConnectionError.MissingFieldData().message))
            }
        })

        connection.receive(javascript)
    }

    /**
     * Tests that error is returned when payload isn't valid JSON
     */
    @Test
    fun testIllegalPayloadResultsInError(){
        val javascript = """abc""".trimIndent()

        val connection = WebViewConnection(mapper, object: JavascriptExecutor{
            override fun executeJavascript(javascript: String) {
                assertTrue(javascript.contains(WebViewConnectionError.IllegalPayloadFormat().message))
            }
        })

        connection.receive(javascript)
    }

    /**
     * Tests that error is returned when a topic handler for specified "topic" hasn't been added
     */
    @Test
    fun testMissingTopicHandlerShouldReturnError(){
        val javascript = """{"topic":"invalid","data":{"key":"value"}}""".trimIndent()

        val connection = WebViewConnection(mapper, object: JavascriptExecutor{
            override fun executeJavascript(javascript: String) {
                assertTrue(javascript.contains(""""topic":"invalid""") && javascript.contains(WebViewConnectionError.MissingTopicHandler().message))
            }
        })

        connection.receive(javascript)
    }

    /**
     * Tests that error is returned when payload contains data that is unrecognizable. This will happen
     * if the data object has @JsonProperty(required = true) field annotations that isn't satisfied.
     */
    @Test
    fun testInvalidDataForHandlerShouldReturnError(){
        val connection = WebViewConnection(mapper, object: JavascriptExecutor{
            override fun executeJavascript(javascript: String) {
                assertTrue(javascript.contains(""""topic":"testTopic"""")
                        && javascript.contains(WebViewConnectionError.InvalidDataForTopicHandler("testTopic").message))
            }
        })

        val javascript = """{"topic":"testTopic","data":{"texttt":"someTextForInvalidKey"}}""".trimIndent()

        connection.addHandler("testTopic") { _: TestTopicData.In, _ ->
            /** no-op */
        }

        connection.receive(javascript)
    }

    private fun getJavaScript(topic: String, expectedTopicData: TopicData.Out) : String {
        val json = JSONObject()

        val jsonData = mapper.writeValueAsString(expectedTopicData)
        val jsonDataObject = JSONObject(jsonData)

        json.put("topic", topic)
        json.put("data", jsonDataObject)

        val javascript =
                """
            javascript:window.dispatchEvent(
                new CustomEvent("nativebridge", {
                        "detail": $json
                    }
                )
            )
            """.trimIndent()

        return javascript
    }
}
