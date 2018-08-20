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

package nl.viasalix.horarium.zermelo

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import javax.security.auth.login.LoginException

class ZermeloInterceptor(private var authCode: String) : Interceptor {
    // Set expiresIn to 0 so session.newRequired will be true and the interceptor will log in before
    // the request and tryLogin() will provide the accessToken
    var session = ZermeloSession(0, "")
        private set

    constructor(authCode: String, serializedSession: String) : this(authCode) {
        session = ZermeloSession(serializedSession)
    }

    lateinit var service: ZermeloService
    var loggingIn = false

    /**
     * tryLogin: Performs tryLogin procedure with the provided auth code
     *
     * @return Login success
     */
    private fun tryLogin(): Boolean {
        loggingIn = true
        val loginResponse = service.login(authCode).execute()
        loggingIn = false
        Log.d("Login Response Success", loginResponse.isSuccessful.toString())

        if (loginResponse.isSuccessful) {
            val body = loginResponse.body()
            Log.d("Login Response", body.toString())
            if (body != null) {
                session.accessToken = body.accessToken
                session.expiresIn = body.expiresIn
                session.newRequired = false
                session.startTimer()
            }

            return true
        }

        return false
    }

    /**
     * intercept: Intercepts the request and adds an authentication header
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        // If the `tryLogin()` function is currently trying to log in, the interceptor should not try to log in ;-)
        // Otherwise, we would get a case of InFINitE REcurSioN
        if (loggingIn) {
            return chain.proceed(chain.request())
        }

        // Check whether a new session is required, and if so, log in
        if (session.newRequired) {
            Log.d("session", "new required")
            val loginSuccess = tryLogin()
            if (!loginSuccess) {
                throw LoginException()
            }
        }

        Log.d("Interceptor", "Logged in")

        // Add the authorization header to the request
        var newRequest = chain.request().newBuilder()
            .header("Authorization", "Bearer ${session.accessToken}")
            .build()
        Log.d("request", newRequest.toString())

        // Make the request
        var response = chain.proceed(newRequest)

        // If the response was unsuccessful, try logging in
        if (response.code() == 401 || response.code() == 403) {
            val loginSuccess = tryLogin()
            if (!loginSuccess) {
                throw LoginException()
            }

            newRequest = chain.request().newBuilder()
                .header("Authorization", "Bearer ${session.accessToken}")
                .build()
            response = chain.proceed(newRequest)
        }

        return response
    }
}