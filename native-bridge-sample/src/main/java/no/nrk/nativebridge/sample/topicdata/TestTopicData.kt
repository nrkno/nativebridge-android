package no.nrk.nativebridge.sample.topicdata

import com.fasterxml.jackson.annotation.JsonProperty
import no.nrk.nativebridge.TopicData

class TestTopicData {
    class In(@JsonProperty(required = true) val text: String) : TopicData.In
    class Out(val value: String) : TopicData.Out
}

