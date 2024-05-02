package dk.scheduling.schedulingfrontend.api.typeadapters

import android.util.Log
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeTypeAdapter :
    JsonSerializer<LocalDateTime>,
    JsonDeserializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_DATE_TIME

    override fun serialize(
        datetime: LocalDateTime,
        typeOfSrc: Type,
        context: JsonSerializationContext,
    ): JsonElement {
        Log.i("LocalDateTimeTypeAdapter", "serialize - Input datetime: $datetime")
        val output =
            ZonedDateTime.ofInstant(
                datetime.atZone(ZoneId.systemDefault()).toInstant(),
                ZoneId.of("UTC"),
            ).toLocalDateTime().atZone(ZoneOffset.UTC).format(formatter)

        Log.i(
            "LocalDateTimeTypeAdapter",
            "serialize - Output datetime: $output",
        )

        return JsonPrimitive(output)
    }

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): LocalDateTime {
        val utc = Instant.parse(json.asString)
        Log.i("LocalDateTimeTypeAdapter", "deserialize - Input datetime: $utc")
        Log.i("LocalDateTimeTypeAdapter", "deserialize - Output datetime: ${utc.atZone(ZoneId.systemDefault()).toLocalDateTime()}")
        return utc.atZone(ZoneId.systemDefault()).toLocalDateTime()
    }
}
