package no.nrk.nativebridge

import com.fasterxml.jackson.annotation.JsonFormat

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
sealed class WebViewConnectionError(val code: Int, val message: String) {
    class IllegalPayloadFormat : WebViewConnectionError(1, "Illegal payload format")
    class MissingFieldTopic : WebViewConnectionError(2, "Missing field: 'topic'")
    class MissingFieldData : WebViewConnectionError(3, "Missing field: 'data'")
    class MissingTopicHandler : WebViewConnectionError(4, "Missing topic handler")
    class InvalidDataForTopicHandler(topic: String) : WebViewConnectionError(5, "Invalid data for topic. Excepted data topic '$topic'")
}
