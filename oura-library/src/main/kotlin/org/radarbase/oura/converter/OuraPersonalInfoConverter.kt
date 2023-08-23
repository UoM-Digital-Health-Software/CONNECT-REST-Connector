package org.radarbase.oura.converter

import com.fasterxml.jackson.databind.JsonNode
import org.radarcns.connector.oura.OuraPersonalInfo
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.OffsetDateTime
import org.radarbase.oura.user.User

class OuraPersonalInfoConverter(
    private val topic: String = "connect_oura_personal_info",
) : OuraDataConverter {
    override fun processRecords(
        root: JsonNode,
        user: User
    ): Sequence<Result<TopicData>> {
        return sequenceOf(
            runCatching {
                TopicData(
                    key = user.observationKey,
                    topic = topic,
                    value = root.toPersonalInfo(),
                )
            })
    }

    private fun JsonNode.toPersonalInfo(): OuraPersonalInfo {
        val data = this
        return OuraPersonalInfo.newBuilder().apply {
            id = data.get("id").textValue()
            age = data.get("age").intValue()
            weight = data.get("weight").floatValue()
            height = data.get("height").floatValue()
            biologicalSex = data.get("biological_sex").textValue()
            email = data.get("email").textValue()
        }.build()
    }

    companion object {
        val logger = LoggerFactory.getLogger(OuraPersonalInfoConverter::class.java)
    }
}
