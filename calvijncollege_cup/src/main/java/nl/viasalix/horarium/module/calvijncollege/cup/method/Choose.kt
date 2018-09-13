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
import nl.viasalix.horarium.module.calvijncollege.cup.data.Choice
import org.jsoup.Jsoup

data class Choose(override val success: Boolean) : CUPMethod<Any>() {
    override val result: Any? = null

    companion object {
        fun execute(cupClient: CUPClient, internalId: Int): Choose {
            // TODO: Check if successfully signed in

            val call = cupClient.createCall("RoosterForm.aspx", "POST", mapOf(
                    "__EVENTARGUMENT" to internalId.toString(),
                    "__SCROLLPOSITIONX" to "0",
                    "__SCROLLPOSITIONY" to "0",
                    "lastPosition" to "",
                    "txtLLtekst" to "",
                    "hiddenlesnr" to "",
                    "rememberClickY" to "185"
            ), AspFieldMode.EXCEPT_EVENTARGUMENT)
            val response = call.execute()

            if (response?.body() == null) return Choose(false).also { it.failReason = "E_Choose_BodyNull" }
            val doc = Jsoup.parse(response.body()!!.string())
            cupClient.session.extractAspFields(doc)

            val prekolomImgs = doc.select(".prekolom > img");
            if (prekolomImgs.isNotEmpty()) {
                val img = prekolomImgs.first()
                if (img.attr("src")!!.contentEquals("images/infoError.png") && img.hasAttr("onmouseover")) {
                    val errorMessage = img
                            .attr("onmouseover")
                            .replace("showHelpText('", "")
                            .replace("',event);", "")
                    return Choose(false).also { it.failReason = errorMessage }
                }
            }

            return Choose(true)
        }

        fun execute(cupClient: CUPClient, choice: Choice) = execute(cupClient, choice.internalId)
    }
}