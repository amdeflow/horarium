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

import android.net.Uri
import com.google.gson.GsonBuilder
import nl.viasalix.horarium.zermelo.model.Announcement
import nl.viasalix.horarium.zermelo.model.Appointment
import nl.viasalix.horarium.zermelo.model.ParentTeacherNight
import nl.viasalix.horarium.zermelo.model.ZermeloResponse
import nl.viasalix.horarium.zermelo.utils.DateUtils
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date

class ZermeloInstance(
    schoolName: String,
    authCode: String,
    serializedSession: String = "",
    apiVer: Int = 3
) {
    private var interceptor: ZermeloInterceptor = if (serializedSession.isNotEmpty()) {
        ZermeloInterceptor(authCode, serializedSession)
    } else {
        ZermeloInterceptor(authCode)
    }

    private val zermeloService: ZermeloService
    private val baseUrl = Uri.Builder().scheme("https")
        .encodedAuthority("$schoolName.zportal.nl")
        .encodedPath("api/v$apiVer/")
        .toString()
    private val zermeloClient: OkHttpClient
    private val retrofit: Retrofit

    init {
        zermeloClient = OkHttpClient.Builder().addInterceptor(interceptor).build()

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().registerTypeAdapter(
                        Date::class.java,
                        DateUtils.DateTypeAdapter()
                    ).create()
                )
            ).client(zermeloClient).build()

        zermeloService = retrofit.create(ZermeloService::class.java)
        interceptor.service = zermeloService
    }

    fun getAppointments(
        from: Date? = DateUtils.startOfWeek(),
        till: Date? = DateUtils.endOfWeek(),
        modifiedSince: Date? = null,
        valid: Boolean? = null,
        cancelled: Boolean? = null,
        includeHidden: Boolean? = null,
        user: String = "~me",
        callback: (List<Appointment>?) -> Unit
    ) {
        zermeloService.getAppointments(
            if (from != null) {
                from.time / 1000
            } else {
                null
            },
            if (till != null) {
                till.time / 1000
            } else {
                null
            },
            if (modifiedSince != null) {
                modifiedSince.time / 1000
            } else {
                null
            },
            valid,
            cancelled,
            includeHidden,
            user
        ).enqueue(object : Callback<ZermeloResponse<Appointment>> {
            override fun onResponse(
                call: Call<ZermeloResponse<Appointment>>?,
                response: Response<ZermeloResponse<Appointment>>?
            ) {
                if (response != null) {
                    if (response.isSuccessful) {
                        callback(response.body()?.response?.data)
                    } else {
                        callback(null)
                    }
                }
            }

            override fun onFailure(call: Call<ZermeloResponse<Appointment>>?, t: Throwable?) {
                callback(null)
            }
        })
    }

    fun getAnnouncements(
        current: Boolean? = null,
        user: String = "~me",
        callback: (List<Announcement>?) -> Unit
    ) {
        val response = zermeloService.getAnnouncements(current, user)
            .enqueue(object : Callback<ZermeloResponse<Announcement>> {
                override fun onResponse(
                    call: Call<ZermeloResponse<Announcement>>,
                    response: Response<ZermeloResponse<Announcement>>
                ) {
                    if (response.isSuccessful) {
                        callback(response.body()?.response?.data)
                    } else {
                        callback(null)
                    }
                }

                override fun onFailure(
                    call: Call<ZermeloResponse<Announcement>>,
                    t: Throwable
                ) {
                    callback(null)
                }
            })
    }

    fun getParentTeacherNights(callback: (List<ParentTeacherNight>?) -> Unit) {
        val response = zermeloService.getParentTeacherNights()
            .enqueue(object : Callback<ZermeloResponse<ParentTeacherNight>> {
                override fun onResponse(
                    call: Call<ZermeloResponse<ParentTeacherNight>>,
                    response: Response<ZermeloResponse<ParentTeacherNight>>
                ) {
                    if (response.isSuccessful) {
                        callback(response.body()?.response?.data)
                    } else {
                        callback(null)
                    }
                }

                override fun onFailure(
                    call: Call<ZermeloResponse<ParentTeacherNight>>,
                    t: Throwable
                ) {
                    callback(null)
                }
            })
    }

    fun serializeSession(): String = interceptor.session.serializeToJson()
}