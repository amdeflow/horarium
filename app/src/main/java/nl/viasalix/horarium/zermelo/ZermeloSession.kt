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

import com.google.gson.Gson
import nl.viasalix.horarium.zermelo.utils.DateUtils
import java.util.Date
import java.util.Timer
import java.util.TimerTask

class ZermeloSession {
    private var expirationTimer = Timer()
    var newRequired = false
    var expiresIn: Long
    var accessToken: String

    private val gson by lazy {
        Gson().newBuilder()
            .registerTypeAdapter(Date::class.java, DateUtils.DateTypeAdapter())
            .create()
    }

    data class ZermeloSessionSettings(
        val expirationDate: Date,
        val accessToken: String
    )

    constructor(expiresIn: Long, accessToken: String) {
        this.expiresIn = expiresIn
        this.accessToken = accessToken
    }

    constructor(serializedSession: String) {
        val settings: ZermeloSessionSettings = gson.fromJson(
            serializedSession,
            ZermeloSessionSettings::class.java
        )

        this.newRequired = false
        this.accessToken = settings.accessToken
        this.expiresIn = settings.expirationDate.time - System.currentTimeMillis() / 1000

        startTimer()
    }

    init {
        startTimer()
    }

    fun serializeToJson(): String {
        val settings = ZermeloSessionSettings(
            Date(System.currentTimeMillis() + expiresIn * 1000),
            accessToken
        )

        return gson.toJson(settings, ZermeloSessionSettings::class.java)
    }

    fun startTimer() {
        expirationTimer.schedule(object : TimerTask() {
            override fun run() {
//                newRequired = true
            }
        }, expiresIn * 1000)
    }
}