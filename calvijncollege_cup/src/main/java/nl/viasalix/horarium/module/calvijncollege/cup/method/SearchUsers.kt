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

import nl.viasalix.horarium.module.calvijncollege.cup.CUPClient
import nl.viasalix.horarium.module.calvijncollege.cup.CUPMethod
import org.jsoup.Jsoup

/**
 * @param result A [Map] where the key is the internal username identifier. The value is meant to be presented to the user.
 */
class SearchUsers(override val success: Boolean, override val result: Map<String, String> = emptyMap()) :
    CUPMethod<Map<String, String>>() {
    companion object {
        fun execute(cupClient: CUPClient, surnameFirstLetters: String): SearchUsers {
            if (!cupClient.checkSession()) return SearchUsers(false).also { it.failReason = "E_CupClient_SessionExpired" }

            val extraFields = mapOf(
                "_nameTextBox" to surnameFirstLetters,
                "_zoekButton" to "Zoek",
                "numberOfLettersField" to "3"
            )
            val call = cupClient.createCall("Default.aspx", "POST", extraFields)
            val response = call.execute()

            if (response.body() == null) return SearchUsers(false).also { it.failReason = "E_SearchUsers_BodyNull" }
            val body = response.body()!!.string()

            val doc = Jsoup.parse(body)
            cupClient.session.extractAspFields(doc)

            for (errContainer in doc.select(".clsError")) {
                if (errContainer.attr("isvalid").equals("False")) {
                    return SearchUsers(false).also { it.failReason = "E_SearchUsers_Plain_" + errContainer.text() }
                }
            }

            val names = doc
                .select("#_nameDropDownList option")
                .map { it.`val`() to it.text() }.toMap()

            return SearchUsers(true, names)
        }
    }
}