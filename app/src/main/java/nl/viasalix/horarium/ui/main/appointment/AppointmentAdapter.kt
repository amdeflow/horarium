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

package nl.viasalix.horarium.ui.main.appointment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import nl.viasalix.horarium.R
import nl.viasalix.horarium.zermelo.model.Appointment
import nl.viasalix.horarium.zermelo.utils.DateUtils
import java.util.Calendar

class AppointmentAdapter(private val schedule: MutableList<Appointment>) :
    RecyclerView.Adapter<AppointmentViewHolder>() {

    init {
        schedule.sortWith(compareBy(Appointment::start))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val appointment = LayoutInflater.from(parent.context).inflate(R.layout.appointment, parent, false)
        return AppointmentViewHolder(appointment)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = schedule[position]

        // Disable isRecyclable so the item's position will not change when the user scrolls
        holder.setIsRecyclable(false)

        if (position > 0) {
            if (DateUtils.isOtherDay(schedule[position].start, schedule[position - 1].start)) {
                holder.day.visibility = View.VISIBLE
                holder.day.text = DateUtils.dayToString(appointment.start)
            }
        } else if (position == 0) {
            holder.day.visibility = View.VISIBLE
            holder.day.text = DateUtils.dayToString(appointment.start)
        }

        holder.endSlot.text = appointment.endTimeSlot.toString()
        holder.rooms.text = appointment.locations.joinToString()
        holder.startSlot.text = appointment.startTimeSlot.toString()
        holder.subjects.text = appointment.subjects.joinToString()
        holder.teachers.text = appointment.teachers.joinToString()
        holder.time.text = {
            val start = Calendar.getInstance()
            val end = Calendar.getInstance()
            start.timeInMillis = appointment.start * 1000
            end.timeInMillis = appointment.end * 1000

            "${start[Calendar.HOUR_OF_DAY]}:${start[Calendar.MINUTE]} \u2015 ${end[Calendar.HOUR_OF_DAY]}:${end[Calendar.MINUTE]}"
        }()

        if (appointment.endTimeSlot == appointment.startTimeSlot) {
            holder.endSlot.visibility = View.INVISIBLE
        }

        // TODO: Clean up this tiny mess and make it nicer
        holder.infoChipGroup.visibility = View.GONE
        if (appointment.cancelled) {
            val oldtext = holder.subjects.text
            holder.subjects.text = "$oldtext CANCELLED"
        }
    }

    override fun getItemCount(): Int = schedule.size
}