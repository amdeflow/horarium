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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import nl.viasalix.horarium.R
import nl.viasalix.horarium.databinding.ScheduleFragmentBinding

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

        return binding.root
    }
}
