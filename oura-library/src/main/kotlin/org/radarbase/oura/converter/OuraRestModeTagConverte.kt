package org.radarbase.oura.converter

import com.fasterxml.jackson.databind.JsonNode
import org.radarbase.oura.user.User
import org.radarcns.connector.oura.OuraTag
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.OffsetDateTime

class OuraRestModeTagConverter(
    private val topic: String = "connect_oura_tag",
) : OuraDataConverter {
    override fun processRecords(
        root: JsonNode,
        user: User,
    ): Sequence<Result<TopicData>> {
        val array = root.get("data")
            ?: return emptySequence()
        return array.asSequence()
            .flatMap {
                val episodes = it.get("episodes")
                val data = it
                if (episodes == null) {
                    emptySequence()
                } else {
                    episodes.asSequence()
                        .flatMap {
                            val tags = it.get("tags")
                            val startTime = OffsetDateTime.parse(it["timestamp"].textValue())
                            val startInstant = startTime.toInstant()
                            tags.asSequence().mapCatching { 
                                TopicData(
                                    key = user.observationKey,
                                    topic = topic,
                                    offset = startInstant.toEpochMilli(),
                                    value = data.toTag(startInstant, it.textValue()),
                                ) 
                             }
                        }
                }
            }
    }

    private fun JsonNode.toTag(
        startTime: Instant,
        tagString: String,
    ): OuraTag {
        val data = this
        return OuraTag.newBuilder().apply {
            time = startTime.toEpochMilli() / 1000.0
            timeReceived = System.currentTimeMillis() / 1000.0
            id = data.get("id").textValue()
            day = data.get("start_day").textValue()
            text = "rest_mode_period"
            tag = tagString
        }.build()
    }

    companion object {
        val logger = LoggerFactory.getLogger(OuraRestModeTagConverter::class.java)
    }
}
