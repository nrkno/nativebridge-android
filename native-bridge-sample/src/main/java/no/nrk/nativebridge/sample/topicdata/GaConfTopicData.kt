package no.nrk.nativebridge.sample.topicdata

import no.nrk.nativebridge.TopicData

class GaConfTopicData {
    class In : TopicData.In
    class Out(val cid: String) : TopicData.Out
}