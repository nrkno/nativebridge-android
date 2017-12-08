package no.nrk.nativebridge;

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.json.JSONException
import org.json.JSONObject

class WebViewConnection(val objectMapper: ObjectMapper, private val javascriptExecutor: JavascriptExecutor) {

    val generators = mutableMapOf<String, (String) -> Unit>()

    /**
     * Adds a connection handler. Must be used webviews extending NativeBridgeWebView to handle
     * data flow back and forth between webview and native.
     *
     * @param topic the name of the topic to register. Corresponds to the topic used when sending
     * and receiving JSON
     *
     * @param callback the callback lambda
     */
    inline fun <reified T: TopicData.In> addHandler(topic: String, crossinline callback: (T, WebViewConnection) -> Unit) {
        val generator: (String) -> Unit = {
            try {
                val topicData: T = objectMapper.readValue(it, T::class.java)
                callback(topicData, this)
            } catch (exception: JsonMappingException){
                sendError(topic, mutableListOf(WebViewConnectionError.InvalidDataForTopicHandler(topic)))
            }
        }

        generators.put(topic, generator)
    }

    /**
     * Sends data from native to webview
     *
     * @param topic The topic to use when sending data
     * @param data The object that will be serialized and passed to the webview
     */
    fun send(topic: String, data: TopicData.Out) {
        val jsonData = objectMapper.writeValueAsString(data)
        val jsonDataObject = JSONObject(jsonData)
        val jsonObject = JSONObject()

        jsonObject.put("topic", topic)
        jsonObject.put("data", jsonDataObject)

        val javascript =
                """
            javascript:window.dispatchEvent(
                new CustomEvent("nativebridge", {
                        "detail": $jsonObject
                    }
                )
            )
            """.trimIndent()

        javascriptExecutor.executeJavascript(javascript)
    }

    /**
     * Receives data from webview. If data is valid, generator() will be called with the data as
     * parameter.
     *
     * @param payload The JSON payload coming from the webview
     */
    fun receive(payload: String) {
        val json: JSONObject

        try {
            json = JSONObject(payload)
        } catch (exception: JSONException){
            sendError("error", mutableListOf(WebViewConnectionError.IllegalPayloadFormat()))
            return
        }

        val errors = validate(json)

        if (errors.isEmpty()){
            val generator = generators[json.getString("topic")]
            generator!!(json.getString("data"))
        } else {
            if (json.hasTopic()){
                sendError(json.getString("topic"), errors)
            } else {
                sendError("error", errors)
            }
        }
    }

    /**
     * Validates data from webview
     *
     * @param json The JSONObject to validate
     */
    private fun validate(json: JSONObject): MutableList<WebViewConnectionError> {
        val errors = mutableListOf<WebViewConnectionError>()

        if (!json.has("topic")){
            errors.add(WebViewConnectionError.MissingFieldTopic())
        } else {
            if (generators[json.getString("topic")] == null){
                errors.add(WebViewConnectionError.MissingTopicHandler())
            }
        }

        if (!json.has("data")){
            errors.add(WebViewConnectionError.MissingFieldData())
        }

        return errors
    }

    /**
     * Sends an error to the webview. Topic will be determined by the caller, but will either
     * be the topic received from the webview, or "error" if no topic was passed from webview.
     *
     * @param topic the topic to use when communicating errors to the webview
     * @param errors a list of WebViewConnectionErrors that will be serialized and sent to the webview
     */
    fun sendError(topic: String, errors: MutableList<WebViewConnectionError>) {
        send(topic, WebViewConnectionErrors(errors))
    }

    private fun JSONObject.hasTopic(): Boolean =
            this.has("topic") && !this.getString("topic").isBlank()
}

interface JavascriptExecutor {
    fun executeJavascript(javascript: String)
}
