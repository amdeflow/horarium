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

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import nl.viasalix.horarium.R

class AppointmentViewHolder(appointmentView: View) : RecyclerView.ViewHolder(appointmentView) {
    var endSlot: TextView = appointmentView.findViewById(R.id.endSlot)
    var infoChipGroup: ChipGroup = appointmentView.findViewById(R.id.infoChipGroup)
    var rooms: TextView = appointmentView.findViewById(R.id.rooms)
    var startSlot: TextView = appointmentView.findViewById(R.id.startSlot)
    var subjects: TextView = appointmentView.findViewById(R.id.subjects)
    var teachers: TextView = appointmentView.findViewById(R.id.teachers)
    var time: TextView = appointmentView.findViewById(R.id.time)
    var day: TextView = appointmentView.findViewById(R.id.day)
}