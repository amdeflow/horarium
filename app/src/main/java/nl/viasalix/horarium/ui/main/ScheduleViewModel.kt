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

package nl.viasalix.horarium.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.viasalix.horarium.data.AppointmentCustomizations
import nl.viasalix.horarium.data.repository.ScheduleRepository
import nl.viasalix.horarium.utils.DateUtils.getCurrentWeek
import nl.viasalix.horarium.utils.DateUtils.getCurrentYear
import nl.viasalix.horarium.data.zermelo.model.Appointment
import nl.viasalix.horarium.utils.DateUtils.endOfWeek
import nl.viasalix.horarium.utils.DateUtils.startOfWeek
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class ScheduleViewModel internal constructor(
        private val repository: ScheduleRepository,
        initYear: Int,
        initWeek: Int
) : ViewModel() {

    val year = MutableLiveData<Int>()
    val week = MutableLiveData<Int>()
    var injected = false
    private var dbSchedule: LiveData<List<Appointment>>? = null
    private val schedule = MediatorLiveData<List<Appointment>>()

    init {
        this.year.value = initYear
        this.week.value = initWeek
    }

    fun getSchedule() = schedule

    fun updateSchedule() {
        injected = false

        dbSchedule?.also { schedule.removeSource(it) }
        dbSchedule = repository.getAppointmentsFromTill(
                startOfWeek(week.value ?: getCurrentWeek(), year.value ?: getCurrentYear()),
                endOfWeek(week.value ?: getCurrentWeek(), year.value ?: getCurrentYear())
        )
        schedule.addSource(dbSchedule ?: return, schedule::setValue)
    }

    fun injectCustomizations(c: Map<Long, AppointmentCustomizations>) {
        Log.d("hor.ScheduleViewModel", "running injectCustomizations with ${c.size}")

        if (injected || c.isEmpty()) { return }

        doAsync {
            val appointments: List<Appointment> = schedule.value ?: return@doAsync

            for ((instance, customizations) in c) {
                val ai = appointments.indexOfFirst { it.appointmentInstance == instance }
                appointments[ai].customizations = customizations
            }

            uiThread {

                schedule.value = appointments
            }
        }
    }

}
