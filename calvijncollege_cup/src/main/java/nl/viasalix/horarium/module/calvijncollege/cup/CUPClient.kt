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

package nl.viasalix.horarium.module.calvijncollege.cup

import android.util.Log
import nl.viasalix.horarium.module.calvijncollege.cup.data.Session
import nl.viasalix.horarium.module.calvijncollege.cup.method.SearchUsers
import nl.viasalix.horarium.module.calvijncollege.cup.method.SignIn
import okhttp3.Call
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Buffer
import org.jsoup.Jsoup
import java.text.SimpleDateFormat

class CUPClient(host: String = "ccgobb.cupweb6.nl") {
    private val baseUrl = HttpUrl.Builder()
        .scheme("https")
        .host(host)
        .build()

    private lateinit var client: OkHttpClient
    lateinit var session: Session

    fun init() {
        val plainCookieJar = PlainCookieJar()
        client = OkHttpClient.Builder()
            .cookieJar(plainCookieJar)
            .build()

        val request = Request.Builder()
            .url(baseUrl)
            .build()

        val response = client.newCall(request).execute()
        val token = response.request().url().pathSegments()[0]

        val newSession = Session(token)
        newSession.cookies = plainCookieJar.cookies
        newSession.extractAspFields(Jsoup.parse(response.body()!!.string()))
        init(newSession)
    }

    /**
     * First element: success.
     *
     * Second element: fail reason
     */
    fun init(surnameFirstLetters: String, internalUsernameIdentifier: String, pin: String): Pair<Boolean, String> {
        init()

        val searchResult = SearchUsers.execute(this, surnameFirstLetters)

        if (!searchResult.success) {
            return Pair(false, searchResult.failReason)
        }

        if (!searchResult.result.containsKey(internalUsernameIdentifier)) {
            return Pair(false, "E_CUPClient_ProvidedInternalUsernameIdentifierNotFound")
        }

        session.internalUsernameIdentifier = internalUsernameIdentifier
        session.pin = pin

        val signInResult = SignIn.execute(this)

        if (!signInResult.success) {
            return Pair(signInResult.success, signInResult.failReason)
        }

        return Pair(true, "")
    }

    fun init(loadedSession: Session) {
        session = loadedSession
        client = OkHttpClient.Builder()
            .cookieJar(session)
            .build()
    }

    fun createCall(
        endpoint: String,
        method: String = "GET",
        extraFields: Map<String, String> = emptyMap(),
        aspFieldMode: AspFieldMode = AspFieldMode.ALL
    ): Call {
        val urlBuilder = baseUrl.newBuilder()
            .addPathSegments(session.token)
            .addPathSegment(endpoint)

        val requestBuilder = Request.Builder()

        if (method.contentEquals("GET")) {
            requestBuilder.get()

            session.filteredAspFields(aspFieldMode).forEach { urlBuilder.setQueryParameter(it.key, it.value) }
            extraFields.forEach { urlBuilder.setQueryParameter(it.key, it.value) }
        }

        if (method.contentEquals("POST")) {
            val body = FormBody.Builder()
                .also { builder ->
                    session.filteredAspFields(aspFieldMode).forEach { builder.add(it.key, it.value) }
                    extraFields.forEach { builder.add(it.key, it.value) }
                }
                .build()


            requestBuilder.post(body)

            if (Log.isLoggable("CUPClient", Log.DEBUG)) {
                Log.d("CUPClient", "RequestBody: <next message>")
                Buffer().also {
                    body.writeTo(it)
                    Log.d("CUPClient", it.readUtf8())
                }
            }
        }

        val request = requestBuilder
            .url(urlBuilder.build())
            .build()

        return client.newCall(request)
    }

    companion object {
        val appointmentDateFormatter = SimpleDateFormat("d-M-y")
        val appointmentTimeFormatter = SimpleDateFormat("k.m")
    }
}