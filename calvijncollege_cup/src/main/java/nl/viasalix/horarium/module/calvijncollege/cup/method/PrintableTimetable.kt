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

package nl.viasalix.horarium.module.calvijncollege.cup.method

import nl.viasalix.horarium.module.calvijncollege.cup.AspFieldMode
import nl.viasalix.horarium.module.calvijncollege.cup.CUPClient
import nl.viasalix.horarium.module.calvijncollege.cup.CUPMethod
import nl.viasalix.horarium.module.calvijncollege.cup.data.cup.model.HistoryOption
import nl.viasalix.horarium.module.calvijncollege.cup.data.cup.model.Option
import org.jsoup.Jsoup
import java.text.ParseException
import java.util.Date
import java.util.HashMap

/**
 * @param result A [Map] where the key is the week number and where the value is a [List] of [HistoryOption]s.
 */
class PrintableTimetable(
    override val success: Boolean,
    override val result: Map<Int, List<HistoryOption>> = emptyMap()
) : CUPMethod<Map<Int, List<HistoryOption>>>() {
    companion object {
        fun execute(cupClient: CUPClient): PrintableTimetable {
            if (!cupClient.checkSession()) return PrintableTimetable(false).also { it.failReason = "E_CupClient_SessionExpired" }

            // TODO: Check if successfully signed in

            // Make sure we navigate to "Printbaar Rooster" via the page RoosterForm.aspx
            val primingCall = cupClient.createCall("RoosterForm.aspx", "GET", aspFieldMode = AspFieldMode.NONE)
            primingCall.execute()?.body()?.let {
                cupClient.session.extractAspFields(Jsoup.parse(it.string()))
            }

            // A request to RoosterForm.aspx is executed, but a HTTP redirect will be performed by the CUPweb server
            // This request is used to make the most wide selection of the history
            val baseSelectionCall = cupClient.createCall(
                "RoosterForm.aspx", "POST", mapOf(
                    "ToPrintableRooster" to "Printbaar Rooster"
                )
            )
            val baseSelectionResult = baseSelectionCall.execute()

            if (baseSelectionResult?.body() == null) return PrintableTimetable(false).also {
                it.failReason = "E_PrintableTimetable_BaseBodyNull"
            }
            val baseSelectionDoc = Jsoup.parse(baseSelectionResult.body()!!.string())
            cupClient.session.extractAspFields(baseSelectionDoc)

            val from = baseSelectionDoc.getElementById("dropDatumVan").select("> option").last().`val`()
            val to = baseSelectionDoc.getElementById("dropDatumTot").select("> option").first().`val`()

            // This is the request that requests the 'real' history data
            val realHistoryCall = cupClient.createCall(
                "PrintableRooster.aspx", "POST", mapOf(
                    "dropDatumVan" to from,
                    "dropDatumTot" to to
                )
            )
            val realHistoryResponse = realHistoryCall.execute()

            if (realHistoryResponse?.body() == null) return PrintableTimetable(false).also {
                it.failReason = "E_PrintableTimetable_RealHistoryBodyNull"
            }
            val realHistoryDoc = Jsoup.parse(realHistoryResponse.body()!!.string())
            cupClient.session.extractAspFields(realHistoryDoc)

            val historyOptions: MutableMap<Int, MutableList<HistoryOption>> = HashMap()
            val historyTable = realHistoryDoc.select(".StandaardTabel").first()
                ?: return PrintableTimetable(false).also { it.failReason = "E_PrintableTimetable_InvalidDocument" }

            var justFoundWeekMark = false
            var lastKnownDate: Date?
            var optionsForCurrentWeek: MutableList<HistoryOption> = ArrayList()

            historyTable.select("> tbody > tr").also { it.removeAt(0) }.forEach { tableRow ->
                val tableColumns = tableRow.select("> td")

                if (tableColumns.size == 1) {
                    val weekMark = tableColumns[0].text().substring(7).toIntOrNull() ?: 1
                    if (historyOptions.containsKey(weekMark)) {
                        optionsForCurrentWeek = historyOptions[weekMark]!!
                    } else {
                        optionsForCurrentWeek = ArrayList()
                        historyOptions[weekMark] = optionsForCurrentWeek
                    }
                    justFoundWeekMark = true
                } else if (!justFoundWeekMark) {
                    lastKnownDate = try {
                        // Parse the dateString and remove the unnecessary column
                        CUPClient.appointmentDateFormatter.parse(tableColumns.removeAt(0).text().replace("\u00a0", ""))
                    } catch (pe: ParseException) {
                        null
                    }

                    // Remove the second column which too isn't necessary
                    tableColumns.removeAt(0)

                    var slot = 1
                    tableColumns.forEach { tableColumn ->
                        val columnText = tableColumn.text().replace("\u00a0", "")
                        if (columnText.isNotBlank() && lastKnownDate != null) {
                            val option = Option.parse(tableColumn, Option.Pattern.PrintableTimetable)
                            if (option != null) {
                                optionsForCurrentWeek.add(HistoryOption(lastKnownDate!!, slot, option))
                            }
                        }

                        slot++
                    }
                } else justFoundWeekMark = false
            }

            return PrintableTimetable(true, historyOptions)
        }
    }
}
