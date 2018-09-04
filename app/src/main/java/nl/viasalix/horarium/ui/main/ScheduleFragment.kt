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
import nl.viasalix.horarium.zermelo.ZermeloInstance
import nl.viasalix.horarium.zermelo.model.Appointment
import nl.viasalix.horarium.zermelo.utils.DateUtils
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.selector
import org.jetbrains.anko.uiThread
import android.app.AlertDialog
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.MenuItem
import android.widget.EditText

class ScheduleFragment : Fragment() {

    companion object {
        fun newInstance() = ScheduleFragment()
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: ScheduleViewModel
    private lateinit var viewAdapter: AppointmentAdapter
    private lateinit var db: HorariumDatabase
    private lateinit var instance: ZermeloInstance

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> { refresh(); true }
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
        viewModel = ViewModelProviders.of(this).get(ScheduleViewModel::class.java)

        val binding: ScheduleFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.schedule_fragment, container, false)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        val currentUser = activity?.defaultSharedPreferences?.getString(getString(R.string.SP_KEY_CURRENT_USER), "")
        val currentUserSp = activity?.getSharedPreferences(currentUser, Context.MODE_PRIVATE)
        val accessToken = currentUserSp?.getString(getString(R.string.SP_KEY_ACCESS_TOKEN), "")
        val schoolName = currentUserSp?.getString(getString(R.string.SP_KEY_SCHOOL_NAME), "")

        db = Room.databaseBuilder<HorariumDatabase>(
            activity!!.applicationContext,
            HorariumDatabase::class.java,
            "horarium-db_$currentUser"
        ).build()

        instance = ZermeloInstance(
            schoolName = schoolName!!,
            accessToken = accessToken!!
        )

        viewAdapter = AppointmentAdapter(emptyList<Appointment>().toMutableList())

        recyclerView = binding.root.findViewById(R.id.scheduleRecyclerView)!!
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = viewAdapter
        recyclerView.itemAnimator = DefaultItemAnimator()

        viewModel.schedule.observe(this, Observer<MutableList<Appointment>> { appointments ->
            viewAdapter.updateSchedule(appointments) {
                viewAdapter.notifyDataSetChanged()
            }
            recyclerView.recycledViewPool.clear()
        })

        viewModel.selectedWeek.observe(this, Observer<Int> { _ -> refresh() })

        activity?.findViewById<FloatingActionButton>(R.id.weekSelector)?.setOnClickListener {
            val weeks = listOf(
                DateUtils.threeWeeksAgo(),
                DateUtils.twoWeeksAgo(),
                DateUtils.previousWeek(),
                DateUtils.currentWeek(),
                DateUtils.nextWeek(),
                DateUtils.inTwoWeeks(),
                DateUtils.inThreeWeeks()
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
                else customWeekDialog { done, week ->
                    if (done) {
                        viewModel.selectedWeek.value = week
                    }
                }
            }
        }

        return binding.root
    }

    private fun customWeekDialog(onDoneCallback: (Boolean, Int) -> Unit) {
        val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.ThemeOverlay_MaterialComponents_Dialog))
        builder.setTitle("Enter a week number")

        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.maxWidth = 2

        builder.setView(input)

        builder.setPositiveButton(getString(android.R.string.ok)) { _, _ ->
            if (input.text.toString().toInt() >= 0)
                onDoneCallback(true, input.text.toString().toInt())
            else
                customWeekDialog(onDoneCallback)
        }

        builder.setNegativeButton(getString(android.R.string.cancel)) { dialog, _ ->
            dialog.cancel()

            onDoneCallback(false, -1)
        }

        builder.show()
    }

    private fun refresh() {
        instance.getAppointments(viewModel.selectedWeek.value!!) { appointments ->
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
            val dbAppointments = db.appointmentDao().getAppointmentsFromTill(
                DateUtils.startOfWeek(viewModel.selectedWeek.value!!).time / 1000,
                DateUtils.endOfWeek(viewModel.selectedWeek.value!!).time / 1000
            )

            uiThread {
                viewModel.schedule.value = dbAppointments.sortedWith(compareBy(Appointment::start)).toMutableList()

                if (viewModel.selectedWeek.value == DateUtils.currentWeek()) {
                    scrollToToday()
                }
            }
        }
    }

    private fun scrollToToday() {
        val firstToday = viewModel.schedule.value?.find { android.text.format.DateUtils.isToday(it.start * 1000) }
        val position = viewModel.schedule.value?.indexOf(firstToday)
        view?.findViewById<RecyclerView>(R.id.scheduleRecyclerView)?.scrollToPosition(position!!)
    }
}
