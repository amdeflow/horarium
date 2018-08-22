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

import nl.viasalix.horarium.zermelo.model.Announcement
import nl.viasalix.horarium.zermelo.model.Appointment
import nl.viasalix.horarium.zermelo.model.ParentTeacherNight
import nl.viasalix.horarium.zermelo.model.ZermeloAuthResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ZermeloService {
    @FormUrlEncoded
    @POST("oauth/token")
    fun login(
        @Field("code") code: String,
        @Field("grant_type") grantType: String = "authorization_code"
    ):
        Call<ZermeloAuthResponse>

    @GET("announcements")
    fun getAnnouncements(
        @Query("current") current: Boolean?,
        @Query("user") user: String = "~me"
    ):
        Call<ZermeloResponse<Announcement>>

    @GET("appointments")
    fun getAppointments(
        @Query("start") start: Long?,
        @Query("end") end: Long?,
        @Query("modifiedSince") modifiedSince: Long?,
        @Query("valid") valid: Boolean?,
        @Query("cancelled") cancelled: Boolean?,
        @Query("includeHidden") includeHidden: Boolean?,
        @Query("user") user: String = "~me"
    ):
        Call<ZermeloResponse<Appointment>>

    // This is just wrong
    @GET("parentteachernights")
    fun getParentTeacherNights(): Call<ZermeloResponse<ParentTeacherNight>>
}