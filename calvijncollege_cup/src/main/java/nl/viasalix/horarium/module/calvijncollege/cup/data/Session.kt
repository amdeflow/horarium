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

import nl.viasalix.horarium.module.calvijncollege.cup.AspFieldMode
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import org.jsoup.nodes.Document

/**
 * Session storage.
 * @param token Session token.
 * @param internalUsernameIdentifier Internal username identifier.
 * @param pin Pin for the given [internalUsernameIdentifier]. Usually a four-digit string.
 */
data class Session(val token: String, var internalUsernameIdentifier: String = "", var pin: String = "") : CookieJar {

    private val aspFields: MutableMap<String, String> = HashMap()
    var cookies: MutableList<Cookie> = ArrayList()

    fun extractAspFields(doc: Document) {
        putElementValueIfPresent(doc, "__EVENTTARGET", aspFields)
        putElementValueIfPresent(doc, "__EVENTARGUMENT", aspFields)
        putElementValueIfPresent(doc, "__VIEWSTATE", aspFields)
        putElementValueIfPresent(doc, "__VIEWSTATEGENERATOR", aspFields)
        putElementValueIfPresent(doc, "__EVENTVALIDATION", aspFields)
    }

    private fun putElementValueIfPresent(
        doc: Document,
        id: String,
        map: MutableMap<String, String>,
        defaultValue: String = ""
    ) {
        doc.getElementById(id).also {
            map[id] = if (it == null) defaultValue else it.`val`()
        }
    }

    fun filteredAspFields(aspFieldMode: AspFieldMode): Map<String, String> {
        return when (aspFieldMode) {
            AspFieldMode.NONE -> emptyMap()
            AspFieldMode.EXCEPT_EVENTARGUMENT -> aspFields.filter { !it.key.contentEquals("__EVENTARGUMENT") }
            AspFieldMode.ALL -> aspFields
        }
    }

    override fun loadForRequest(url: HttpUrl): MutableList<Cookie> = cookies

    override fun saveFromResponse(url: HttpUrl, cookies: MutableList<Cookie>) {
        this.cookies = cookies
    }
}