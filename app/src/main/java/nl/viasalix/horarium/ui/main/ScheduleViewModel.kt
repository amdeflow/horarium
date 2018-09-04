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

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.viasalix.horarium.zermelo.model.Appointment
import java.util.Calendar

class ScheduleViewModel : ViewModel() {
    val schedule = MutableLiveData<MutableList<Appointment>>()
    var selectedWeek = MutableLiveData<Int>()

    init {
        selectedWeek.value = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
    }
}
