package nl.viasalix.horarium.converters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import nl.viasalix.horarium.utils.DateUtils
import nl.viasalix.horarium.utils.DateUtils.unixSeconds
import java.util.*

class DateTypeAdapter : TypeAdapter<Date>() {
    override fun read(`in`: JsonReader): Date = DateUtils.unixSecondsToDate(`in`.nextLong())
    override fun write(out: JsonWriter, value: Date) {
        out.value(value.unixSeconds())
    }
}
