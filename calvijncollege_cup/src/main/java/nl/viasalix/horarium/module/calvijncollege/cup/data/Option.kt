/*
 * Copyright 2018 Jochem Broekhoff
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

package nl.viasalix.horarium.module.calvijncollege.cup.data

import org.jsoup.nodes.Element

/**
 * @param subject Subject.
 * @param room Room.
 * @param teacher Teacher.
 * @param extra Extra bits, not specifically related to [subject], [room] or [teacher].
 * @param message A message for this [Option]. Not used at the moment.
 * @param info Information for this [Option]. Usually describes what will happen in this lesson.
 * @param availablePlaces Amount of available places. A negative number usually indicates that this option is full.
 */
data class Option(
    val subject: String,
    val room: String,
    val teacher: String,
    val extra: String = "",
    val message: String = "",
    val info: String = "",
    val availablePlaces: Int = -1
) {

    enum class Pattern {
        Appointments,
        PrintableTimetable
    }

    companion object {
        fun parse(input: Element, pattern: Pattern = Pattern.Appointments): Option? {
            val isPrintableTTFormat = pattern == Pattern.PrintableTimetable

            var subject = ""
            var room = ""
            var teacher = ""
            var extra = ""
            var message = ""
            var info = ""
            var availablePlaces = -1

            input.text().split(' ').forEachIndexed { index, part ->
                if (part.startsWith("[") && part.endsWith("]") && part.length >= 3) {
                    availablePlaces = part.substring(0, part.length).toIntOrNull() ?: -1
                } else {
                    when (index) {
                        0 ->
                            if (isPrintableTTFormat) teacher = part
                            else subject = part
                        1 ->
                            if (isPrintableTTFormat) subject = part
                            else when (part) {
                                "toa", "deb" -> extra = part
                                else -> room = part
                            }
                        2 ->
                            if (isPrintableTTFormat) when (part) {
                                "toa", "deb" -> extra = part
                                else -> room = part
                            }
                            else when (extra) {
                                "toa", "deb" -> room = part
                                else -> teacher = part
                            }
                        3 ->
                            when (extra) {
                                "toa", "deb" ->
                                    if (isPrintableTTFormat) room = part
                                    else teacher = part
                            }
                    }
                }
            }

            val onclick = input.attr("onclick")
            if (onclick.isNotBlank() && onclick.startsWith("gekozenlesClicked(") && onclick.endsWith(", event);")) {
                // TODO: Extract message (not really important, it is not used that often)
            }

            val images = input.select("img")!!
            if (images.isNotEmpty()) {
                info = images.first()
                    .attr("onmouseover")
                    .replace("showHelpText('", "")
                    .replace("',event);", "")
            }

            return Option(subject, room, teacher, extra, message, info, availablePlaces)
        }
    }
}