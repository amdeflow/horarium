/*
 * Copyright 2018 Rutger Broekhoff
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.viasalix.horarium.zermelo.utils

import android.text.format.DateUtils
import android.util.Log
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    fun unixSecondsToDate(timestamp: Long) = Date(timestamp * 1000)
    fun Date.unixSeconds() = this.time / 1000

    fun startOfWeek(week: Int): Date {
        val cal = Calendar.getInstance()
        with(cal) {
            set(Calendar.WEEK_OF_YEAR, week)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        Log.d("startOfWeek", "($week): ${cal.time}")

        return cal.time
    }

    fun endOfWeek(week: Int): Date {
        val cal = Calendar.getInstance()
        with(cal) {
            set(Calendar.WEEK_OF_YEAR, week)
            set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        Log.d("endOfWeek", "($week): ${cal.time}")

        return cal.time
    }

    fun getCurrentUnixSeconds() = System.currentTimeMillis() / 1000

    fun isOtherDay(currentTime: Long, oldTime: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTime * 1000
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        calendar.timeInMillis = oldTime * 1000
        val oldDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        return currentDayOfWeek != oldDayOfWeek
    }

    fun dayToString(timestamp: Long): String {
        var day = ""
        if (DateUtils.isToday(timestamp * 1000)) {
            day = "Today \u2015 "
        }

        day += SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date(timestamp * 1000))

        return day
    }

    fun threeWeeksAgo() = with(Calendar.getInstance()) { add(Calendar.WEEK_OF_YEAR, -3); return@with get(Calendar.WEEK_OF_YEAR) }
    fun twoWeeksAgo() = with(Calendar.getInstance()) { add(Calendar.WEEK_OF_YEAR, -2); return@with get(Calendar.WEEK_OF_YEAR) }
    fun previousWeek() = with(Calendar.getInstance()) { add(Calendar.WEEK_OF_YEAR, -1); return@with get(Calendar.WEEK_OF_YEAR) }
    fun currentWeek() = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
    fun nextWeek() = with(Calendar.getInstance()) { add(Calendar.WEEK_OF_YEAR, 1); return@with get(Calendar.WEEK_OF_YEAR) }
    fun inTwoWeeks() = with(Calendar.getInstance()) { add(Calendar.WEEK_OF_YEAR, 2); return@with get(Calendar.WEEK_OF_YEAR) }
    fun inThreeWeeks() = with(Calendar.getInstance()) { add(Calendar.WEEK_OF_YEAR, 3); return@with get(Calendar.WEEK_OF_YEAR) }

    class DateTypeAdapter : TypeAdapter<Date>() {
        override fun read(`in`: JsonReader): Date = unixSecondsToDate(`in`.nextLong())
        override fun write(out: JsonWriter, value: Date) {
            out.value(value.unixSeconds())
        }
    }
}