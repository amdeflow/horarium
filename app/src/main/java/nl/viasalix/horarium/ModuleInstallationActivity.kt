/*
 * Copyright 2018 Jochem Broekhoff
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

package nl.viasalix.horarium

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nl.viasalix.horarium.databinding.ActivityModuleInstallationBinding
import nl.viasalix.horarium.module.ModuleManager
import nl.viasalix.horarium.module.ModuleStatusReport
import nl.viasalix.horarium.ui.module.ModuleItemAdapter
import nl.viasalix.horarium.ui.module.installation.ModuleInstallationViewModel

class ModuleInstallationActivity : AppCompatActivity() {

    private lateinit var viewModel: ModuleInstallationViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var statusReports: Array<ModuleStatusReport> = emptyArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ModuleInstallationViewModel::class.java)

        val binding = DataBindingUtil.setContentView<ActivityModuleInstallationBinding>(
                this, R.layout.activity_module_installation
        )
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        val availableModules = intent.getStringArrayListExtra("availableModules")
        val activeModules = intent.getStringArrayListExtra("activeModules")

        if (availableModules != null && activeModules != null) {
            statusReports = ModuleManager.createStatusReport(availableModules, activeModules)
        }

        viewManager = LinearLayoutManager(this)
        viewAdapter = ModuleItemAdapter(
            statusReports,
            getString(R.string.already_downloaded),
            getString(R.string.must_be_downloaded),
            ::activationStateChanged
        )

        recyclerView = findViewById<RecyclerView>(R.id.module_installation_recyclerView).apply {
            setHasFixedSize(true) // Improved performance. All items will have the same height.
            layoutManager = viewManager
            adapter = viewAdapter
        }

        viewModel.proceedButtonText.value = "skip"
    }

    fun activationStateChanged() {
        if (statusReports.count { it.activated } == 0) {
            viewModel.proceedButtonText.value = "skip"
        } else {
            viewModel.proceedButtonText.value = "proceed"
        }
    }
}
