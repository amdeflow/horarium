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

import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import nl.viasalix.horarium.R
import nl.viasalix.horarium.data.zermelo.model.Appointment
import nl.viasalix.horarium.databinding.ScheduleFragmentBinding
import nl.viasalix.horarium.ui.main.bottomsheets.WeekSelectorDialog
import nl.viasalix.horarium.ui.main.recyclerview.ScheduleAdapter
import nl.viasalix.horarium.utils.DateUtils.getCurrentWeek
import nl.viasalix.horarium.utils.InjectorUtils
import java.util.*

class ScheduleFragment : Fragment() {

    private lateinit var viewModel: ScheduleViewModel
    private lateinit var scheduleView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding = ScheduleFragmentBinding.inflate(inflater, container, false)
        val context = context ?: return binding.root

        val factory = InjectorUtils.provideScheduleViewModelFactory(context)
        viewModel = ViewModelProviders.of(this, factory).get(ScheduleViewModel::class.java)

        val adapter = ScheduleAdapter(context)
        scheduleView = binding.schedule
        scheduleView.adapter = adapter
        // Disable recycling so appointments won't show up twice
        scheduleView.recycledViewPool.setMaxRecycledViews(0, 0)
        subscribeSchedule(adapter)

        activity?.findViewById<FloatingActionButton>(R.id.weekSelector)?.setOnClickListener {
            val sheet = WeekSelectorDialog()
            sheet.setup(viewModel.year.value, viewModel.week.value)
            sheet.onResultCallback = { year, week ->
                viewModel.year.value = year
                viewModel.week.value = week
                viewModel.updateSchedule()
            }
            sheet.show(fragmentManager, "")
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        viewModel.updateSchedule()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> { viewModel.updateSchedule(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun subscribeSchedule(adapter: ScheduleAdapter) {
        viewModel.getSchedule().observe(viewLifecycleOwner, Observer { schedule ->
            if (schedule != null) {
                adapter.submitList(schedule.sortedBy(Appointment::start))
                if (viewModel.week.value == getCurrentWeek()) {
                    Log.v("hor.ScheduleFragment", "scrolling to today")
                    val index = schedule.indexOfFirst { DateUtils.isToday(it.start.time) }
                    Log.v("hor.ScheduleFragment", "scrolling to position $index")
                    try {
                        val layoutManager = scheduleView.layoutManager as LinearLayoutManager?
                        layoutManager?.scrollToPositionWithOffset(index, 0)
                    } catch (e: IllegalArgumentException) {
                        return@Observer
                    }
                }
            }
            else Log.e("hor.ScheduleFragment", "schedule is null")
        })
    }

}
