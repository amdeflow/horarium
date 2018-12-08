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

package nl.viasalix.horarium.utils

import android.content.Context
import android.text.format.DateUtils
import nl.viasalix.horarium.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    fun unixSecondsToDate(timestamp: Long) = Date(timestamp * 1000)
    fun Date.unixSeconds() = this.time / 1000

    fun startOfWeek(week: Int, year: Int): Date {
        val cal = Calendar.getInstance()
        cal.apply {
            set(Calendar.YEAR, year)
            set(Calendar.WEEK_OF_YEAR, week)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return cal.time
    }

    fun endOfWeek(week: Int, year: Int): Date {
        val cal = Calendar.getInstance()

        cal.apply {
            set(Calendar.YEAR, year)
            set(Calendar.WEEK_OF_YEAR, week)
            set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        return cal.time
    }

    fun getCurrentYear() = with(Calendar.getInstance()) { get(Calendar.YEAR) }

    fun getCurrentWeek() = with(Calendar.getInstance()) { get(Calendar.WEEK_OF_YEAR) }

    fun isOtherDay(currentTime: Date, oldTime: Date): Boolean {
        val calendar = Calendar.getInstance()
        calendar.time = currentTime
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        calendar.time = oldTime
        val oldDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        return currentDayOfWeek != oldDayOfWeek
    }

    fun dayToString(timestamp: Date): String {
        return SimpleDateFormat("EEEE", Locale.getDefault()).format(timestamp)
    }

    fun dateToString(context: Context?, timestamp: Date): String {
        return SimpleDateFormat(
                context?.getString(R.string.date_format),
                Locale.getDefault()
        ).format(timestamp)
    }

    fun dayToString(context: Context?, timestamp: Date): String {
        var dayString = ""
        if (DateUtils.isToday(timestamp.time)) {
            dayString = "${context?.getString(R.string.today)} \u2015 "
        }

        dayString += dayToString(timestamp) + ", " + this.dateToString(context, timestamp)
        return dayString.capitalize()
    }

    /**
     * Get week with offset
     * @param week Week to calculate offset from
     * @param year The year week is in
     * @param offset By how many weeks to offset
     * @return Pair consisting of <year, week>
     */
    fun getWeekWithOffset(week: Int, year: Int, offset: Int): Pair<Int, Int> {
        with(Calendar.getInstance()) {
            set(Calendar.YEAR, year)
            set(Calendar.WEEK_OF_YEAR, week)
            add(Calendar.WEEK_OF_YEAR, offset)

            return Pair(
                    get(Calendar.YEAR),
                    get(Calendar.WEEK_OF_YEAR)
            )
        }
    }
}
