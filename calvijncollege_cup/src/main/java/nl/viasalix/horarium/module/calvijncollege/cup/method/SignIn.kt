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
 * @param result Reported user name, can be empty
 */
class SignIn(override val success: Boolean, override val result: String = "") : CUPMethod<String>() {
    companion object {
        fun execute(cupClient: CUPClient): SignIn {
            if (cupClient.session.internalUsernameIdentifier.isEmpty() || cupClient.session.pin.isEmpty()) {
                return SignIn(false).also { it.failReason = "E_SignIn_UsernameOrPinEmpty" }
            }

            val extraFields = mapOf(
                    "_nameDropDownList" to cupClient.session.internalUsernameIdentifier,
                    "_pincodeTextBox" to cupClient.session.pin,
                    "_loginButton" to "Login"
            )
            val call = cupClient.createCall("LogInWebForm.aspx", "POST", extraFields)
            val response = call.execute()

            if (response?.body() == null) return SignIn(false).also { it.failReason = "E_SignIn_BodyNull" }

            val doc = Jsoup.parse(response.body()!!.string())
            cupClient.session.extractAspFields(doc)

            doc.getElementById("_errorLabel").also { errorLabel ->
                if (errorLabel != null && !errorLabel.text().trim().contentEquals("Error Label")) {
                    return SignIn(false).also { it.failReason = errorLabel.text() }
                }
            }

            // TODO: Set a flag like "signedIn" to true, to be used for other methods/calls
            // TODO: More extensive sign in success check

            return SignIn(true, doc.getElementById("_nameLabel").text())
        }
    }
}