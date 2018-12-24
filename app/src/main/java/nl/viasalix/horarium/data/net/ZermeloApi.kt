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

package nl.viasalix.horarium.data.net

import android.util.Log
import com.google.gson.GsonBuilder
import nl.viasalix.horarium.converters.DateTypeAdapter
import nl.viasalix.horarium.data.net.args.GetAppointmentsArgs
import nl.viasalix.horarium.data.zermelo.model.*
import nl.viasalix.horarium.utils.DateUtils.unixSeconds
import nl.viasalix.horarium.utils.RetrofitUtils
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.*

interface ZermeloApi {
    @FormUrlEncoded
    @POST("oauth/token")
    fun login(
        @Field("code") code: String,
        @Field("grant_type") grantType: String = "authorization_code"
    ):
        Call<ZermeloAuthResponse>

    @GET("announcements")
    fun getAnnouncements(
        @Query("current") current: Boolean? = null,
        @Query("user") user: String = "~me"
    ):
        Call<ZermeloResponse<Announcement>>

    @GET("appointments")
    fun getAppointments(
        @Query("start") start: Long? = null,
        @Query("end") end: Long? = null,
        @Query("modifiedSince") modifiedSince: Long? = null,
        @Query("valid") valid: Boolean? = null,
        @Query("cancelled") cancelled: Boolean? = null,
        @Query("includeHidden") includeHidden: Boolean? = null,
        @Query("user") user: String = "~me"
    ):
        Call<ZermeloResponse<Appointment>>

    @GET("parentteachernights")
    fun getParentTeacherNights(): Call<ZermeloResponse<ParentTeacherNight>>

    @GET("users/{id}")
    fun getUser(@Path("id") id: String = "~me"): Call<ZermeloResponse<User>>

    companion object {

        fun buildApi(schoolName: String, accessToken: String): ZermeloApi {
            // TODO: Use Dagger for dependency injection
            val interceptor = ZermeloInterceptor(accessToken)
            val zermeloClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
            val retrofit = Retrofit.Builder()
                    .baseUrl(RetrofitUtils.buildBaseUrl(schoolName))
                    .addConverterFactory(
                            GsonConverterFactory.create(
                                    GsonBuilder().registerTypeAdapter(
                                            Date::class.java,
                                            DateTypeAdapter()
                                    ).create()
                            )
                    ).client(zermeloClient).build()
            return retrofit.create(ZermeloApi::class.java)
        }

        fun ZermeloApi.getAppointmentsWithArgs(args: GetAppointmentsArgs): Call<ZermeloResponse<Appointment>> {
            return getAppointments(
                    start = args.from?.unixSeconds(),
                    end = args.till?.unixSeconds(),
                    modifiedSince = args.modifiedSince?.unixSeconds(),
                    valid = args.valid,
                    cancelled = args.cancelled,
                    includeHidden = args.includeHidden
            )
        }

    }
}
