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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.viasalix.horarium.data.repository.ScheduleRepository
import nl.viasalix.horarium.utils.DateUtils.getCurrentWeek
import nl.viasalix.horarium.utils.DateUtils.getCurrentYear
import nl.viasalix.horarium.data.zermelo.model.Appointment
import nl.viasalix.horarium.utils.DateUtils.endOfWeek
import nl.viasalix.horarium.utils.DateUtils.startOfWeek
import java.util.*

class ScheduleViewModel internal constructor(
        private val scheduleRepository: ScheduleRepository,
        initYear: Int,
        initWeek: Int
): ViewModel() {

    val year = MutableLiveData<Int>()
    val week = MutableLiveData<Int>()
    private var liveSchedule: LiveData<List<Appointment>>? = null
    private val schedule = MediatorLiveData<List<Appointment>>()

    init {
        this.year.value = initYear
        this.week.value = initWeek
    }

    fun getSchedule() = schedule

    fun updateSchedule() {
        liveSchedule?.also { it -> schedule.removeSource(it) }
        liveSchedule = scheduleRepository.getAppointmentsFromTill(
                startOfWeek(week.value ?: getCurrentWeek(), year.value ?: getCurrentYear()),
                endOfWeek(week.value ?: getCurrentWeek(), year.value ?: getCurrentYear())
        )
        schedule.addSource(liveSchedule ?: return, schedule::setValue)
    }

}
