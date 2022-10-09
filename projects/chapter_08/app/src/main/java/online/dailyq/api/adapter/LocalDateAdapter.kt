package online.dailyq.api.adapter

import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDate

object LocalDateAdapter : JsonSerializer<LocalDate>,
    JsonDeserializer<LocalDate> {

    override fun serialize(
        src: LocalDate,
        typeOfSrc: Type,
        context: JsonSerializationContext?
    ): JsonElement {
        src.toString()
        return JsonPrimitive(src.toString())
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LocalDate {
        return LocalDate.parse(json.asString)
    }
}
