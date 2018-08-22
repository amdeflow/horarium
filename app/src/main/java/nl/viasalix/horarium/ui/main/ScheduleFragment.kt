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

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import nl.viasalix.horarium.R
import nl.viasalix.horarium.databinding.ScheduleFragmentBinding
import nl.viasalix.horarium.persistence.HorariumDatabase
import nl.viasalix.horarium.ui.main.appointment.AppointmentAdapter
import nl.viasalix.horarium.zermelo.ZermeloInstance
import nl.viasalix.horarium.zermelo.utils.DateUtils
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class ScheduleFragment : Fragment() {

    companion object {
        fun newInstance() = ScheduleFragment()
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: ScheduleViewModel
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProviders.of(this).get(ScheduleViewModel::class.java)

        val binding: ScheduleFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.schedule_fragment, container, false)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        val db = Room.databaseBuilder<HorariumDatabase>(
            activity!!.applicationContext,
            HorariumDatabase::class.java,
            "horarium-db"
        ).build()

        val currentUser = activity?.defaultSharedPreferences?.getString(getString(R.string.SP_KEY_CURRENT_USER), "")
        val currentUserSp = activity?.getSharedPreferences(currentUser, Context.MODE_PRIVATE)
        val accessToken = currentUserSp?.getString(getString(R.string.SP_KEY_ACCESS_TOKEN), "")
        val schoolName = currentUserSp?.getString(getString(R.string.SP_KEY_SCHOOL_NAME), "")

        val instance = ZermeloInstance(
            schoolName = schoolName!!,
            accessToken = accessToken!!
        )
        instance.getAppointments { appointments ->
            if (appointments != null) {
                doAsync {
                    for (appointment in appointments) {
                        db.appointmentDao().insertAppointment(appointment)
                    }

                    val dbAppointments = db.appointmentDao()
                        .getAppointmentsFromTill(DateUtils.startOfWeek().time / 1000, DateUtils.endOfWeek().time / 1000)

                    uiThread {
                        viewManager = LinearLayoutManager(context)
                        viewAdapter = AppointmentAdapter(dbAppointments.toMutableList())

                        recyclerView = binding.root.findViewById<RecyclerView>(R.id.scheduleRecyclerView)!!.apply {
                            setHasFixedSize(true)
                            layoutManager = viewManager
                            adapter = viewAdapter
                        }
                    }
                }

                viewManager = LinearLayoutManager(context)
                viewAdapter = AppointmentAdapter(appointments.toMutableList())

                recyclerView = activity?.findViewById<RecyclerView>(R.id.scheduleRecyclerView)?.apply {
                    setHasFixedSize(true)
                    layoutManager = viewManager
                    adapter = viewAdapter
                } ?: throw Resources.NotFoundException()
            } else {
                Log.e("ERROR", "Appointments are null!")
            }
        }

        return binding.root
    }
}
