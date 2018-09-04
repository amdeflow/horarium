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

import okhttp3.Interceptor
import okhttp3.Response
import javax.security.auth.login.LoginException

class ZermeloInterceptor(var accessToken: String = "") : Interceptor {
    /**
     * intercept: Intercepts the request and adds an authentication header
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        // Add the authorization header to the request
        val newRequest = chain.request().newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        return chain.proceed(newRequest)
    }
}