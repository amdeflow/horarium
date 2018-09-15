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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import nl.viasalix.horarium.R
import nl.viasalix.horarium.databinding.ScheduleFragmentBinding
import nl.viasalix.horarium.persistence.HorariumDatabase
import nl.viasalix.horarium.ui.main.appointment.AppointmentAdapter
import nl.viasalix.horarium.ui.main.dialogs.CustomWeekDialog
import nl.viasalix.horarium.zermelo.ZermeloInstance
import nl.viasalix.horarium.zermelo.model.Appointment
import nl.viasalix.horarium.zermelo.utils.DateUtils
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.selector
import org.jetbrains.anko.uiThread

class ScheduleFragment : Fragment() {

    companion object {
        fun newInstance() = ScheduleFragment()
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: ScheduleViewModel
    private lateinit var viewAdapter: AppointmentAdapter
    private lateinit var db: HorariumDatabase

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> {
                refresh(); true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: ScheduleFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.schedule_fragment, container, false)
        binding.setLifecycleOwner(this)

        val currentUser = activity?.defaultSharedPreferences?.getString(getString(R.string.SP_KEY_CURRENT_USER), "")
        val currentUserSp = activity?.getSharedPreferences(currentUser, Context.MODE_PRIVATE)
        val accessToken = currentUserSp?.getString(getString(R.string.SP_KEY_ACCESS_TOKEN), "")
        val schoolName = currentUserSp?.getString(getString(R.string.SP_KEY_SCHOOL_NAME), "")

        db = Room.databaseBuilder<HorariumDatabase>(
            activity!!.applicationContext,
            HorariumDatabase::class.java,
            "horarium-db_$currentUser"
        ).build()

        viewModel = ViewModelProviders.of(this).get(ScheduleViewModel::class.java)
        viewModel.instance = ZermeloInstance(
            schoolName = schoolName!!,
            accessToken = accessToken!!
        )
        binding.viewModel = viewModel

        setupRecyclerView(binding.root)

        viewModel.schedule.observe(this, Observer<MutableList<Appointment>> { appointments ->
            viewAdapter.updateSchedule(appointments) {
                viewAdapter.notifyDataSetChanged()
            }
            recyclerView.recycledViewPool.clear()
        })

        viewModel.selectedWeek.observe(this, Observer<Int> { _ -> refresh() })

        activity?.findViewById<FloatingActionButton>(R.id.weekSelector)?.setOnClickListener {
            weekSelector()
        }

        return binding.root
    }

    private fun setupRecyclerView(view: View) {
        viewAdapter = AppointmentAdapter(emptyList<Appointment>().toMutableList())

        recyclerView = view.findViewById(R.id.scheduleRecyclerView)!!
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = viewAdapter
        recyclerView.itemAnimator = DefaultItemAnimator()
    }

    private fun weekSelector() {
        val weeks = listOf(
            DateUtils.getWeekWithOffset(-3),
            DateUtils.getWeekWithOffset(-2),
            DateUtils.getWeekWithOffset(-1),
            DateUtils.getWeekWithOffset(0),
            DateUtils.getWeekWithOffset(1),
            DateUtils.getWeekWithOffset(2),
            DateUtils.getWeekWithOffset(3)
        )

        var selectedIndex = 7

        for (week in weeks) {
            if (week == viewModel.selectedWeek.value!!) {
                selectedIndex = weeks.indexOf(week)
                break
            }
        }

        val weeksText = mutableListOf(
            "Three weeks ago (${weeks[0]})",
            "Two weeks ago (${weeks[1]})",
            "One week ago (${weeks[2]})",
            "This week (${weeks[3]})",
            "Next week (${weeks[4]})",
            "In two weeks (${weeks[5]})",
            "In three weeks (${weeks[6]})",
            "Custom week"
        )

        if (selectedIndex == 7) weeksText[7] = "${weeksText[7]} (${viewModel.selectedWeek.value})"
        weeksText[selectedIndex] = "${weeksText[selectedIndex]} \u2015 selected"

        activity!!.selector("Please select a week", weeksText) { _, i ->
            if (i < 7) viewModel.selectedWeek.value = weeks[i]
            else CustomWeekDialog.show(context) { done, week ->
                if (done) {
                    viewModel.selectedWeek.value = week
                }
            }
        }
    }

    private fun refresh() {
        viewModel.instance.getAppointments(viewModel.selectedWeek.value!!) { appointments ->
            if (appointments != null) {
                doAsync {
                    for (appointment in appointments) {
                        db.appointmentDao().insertAppointment(appointment)
                    }

                    viewAppointments()
                }
            } else {
                viewAppointments()
            }
        }
    }

    private fun viewAppointments() {
        doAsync {
            // Collect appointments and sort
            val dbAppointments = db.appointmentDao().getAppointmentsFromTill(
                DateUtils.startOfWeek(viewModel.selectedWeek.value!!).time / 1000,
                DateUtils.endOfWeek(viewModel.selectedWeek.value!!).time / 1000
            ).asSequence().sortedWith(compareBy(Appointment::start)).toMutableList()

            uiThread {
                viewModel.schedule.value = dbAppointments

                if (viewModel.selectedWeek.value == DateUtils.getWeekWithOffset(0)) {
                    scrollToToday()
                }
            }
        }
    }

    private fun scrollToToday() {
        val firstToday = viewModel.schedule.value?.find { DateUtils.isToday(it.start * 1000) }
        if (firstToday != null) {
            val position = viewModel.schedule.value?.indexOf(firstToday)
            if (position != null) view?.findViewById<RecyclerView>(R.id.scheduleRecyclerView)?.scrollToPosition(position)
        }
    }
}
