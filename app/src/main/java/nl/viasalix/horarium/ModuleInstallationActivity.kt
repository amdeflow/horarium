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

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.play.core.splitinstall.SplitInstallException
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import nl.viasalix.horarium.utils.Constants.SP_KEY_MODULE_INSTALLATION_STATE
import nl.viasalix.horarium.databinding.ActivityModuleInstallationBinding
import nl.viasalix.horarium.module.ModuleManager
import nl.viasalix.horarium.module.ModuleStatusReport
import nl.viasalix.horarium.ui.module.ModuleItemAdapter
import nl.viasalix.horarium.ui.module.installation.ModuleInstallationViewModel
import nl.viasalix.horarium.utils.Constants.SP_KEY_MODULES_ACTIVE
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.uiThread

class ModuleInstallationActivity : AppCompatActivity() {

    companion object {
        const val TAG = "Horarium/ModuleInstall"
        const val STATE_SKIPPED = 0
        const val STATE_DONE_NOTHING_DOWNLOADED = 1
        const val STATE_DONE_MODULES_DOWNLOADED = 2
    }

    private lateinit var viewModel: ModuleInstallationViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var userSp: SharedPreferences
    private var statusReports: Array<ModuleStatusReport> = emptyArray()

    private var listener: SplitInstallStateUpdatedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ModuleInstallationViewModel::class.java)

        val binding = DataBindingUtil.setContentView<ActivityModuleInstallationBinding>(
                this, R.layout.activity_module_installation
        )
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        // Retrieve parameters from Intent extra
        val availableModules = intent.getStringArrayListExtra("availableModules")
        val activeModules = intent.getStringArrayListExtra("activeModules")
        userSp = getSharedPreferences(intent.getStringExtra("userSpName"), Context.MODE_PRIVATE)

        if (availableModules != null && activeModules != null) {
            statusReports = ModuleManager.createStatusReport(availableModules, activeModules)
        }

        viewManager = LinearLayoutManager(this)
        viewAdapter = ModuleItemAdapter(
                statusReports,
                getString(R.string.already_downloaded),
                getString(R.string.has_to_be_downloaded),
                ::activationStateChanged
        )

        recyclerView = findViewById<RecyclerView>(R.id.module_installation_recyclerView).apply {
            setHasFixedSize(true) // Improved performance. All items will have the same height.
            layoutManager = viewManager
            adapter = viewAdapter
        }

        viewModel.proceedButtonText.value = getString(R.string.skip)

        findViewById<Button>(R.id.module_installation_proceed).onClick {
            doAsync {
                val activatedModules = statusReports.filter { it.activated }
                val noActivated = activatedModules.size

                if (noActivated == 0) {
                    userSp.edit { putInt(SP_KEY_MODULE_INSTALLATION_STATE, STATE_SKIPPED) }
                    uiThread { finish() }
                } else {
                    if (statusReports.count { it.installed } == noActivated) {
                        userSp.edit {
                            putInt(SP_KEY_MODULE_INSTALLATION_STATE, STATE_DONE_NOTHING_DOWNLOADED)
                            putStringSet(SP_KEY_MODULES_ACTIVE, activatedModules.map { it.moduleName }.toSet())
                        }
                        uiThread { finish() }
                    } else {
                        startInstallationProcess(activatedModules)
                    }
                }
            }
        }

        // Fire initial activationStateChanged
        activationStateChanged()
    }

    override fun onDestroy() {
        if (listener != null)
            HorariumApplication.manager.unregisterListener(listener)
        super.onDestroy()
    }

    private fun activationStateChanged() {
        if (statusReports.count { it.activated } == 0) {
            viewModel.proceedButtonText.value = getString(R.string.skip)
        } else {
            viewModel.proceedButtonText.value = getString(R.string.proceed)
        }
    }

    private fun startInstallationProcess(modulesToActivate: List<ModuleStatusReport>) {
        val installRequest = SplitInstallRequest.newBuilder().also { builder ->
            modulesToActivate.filter { !it.installed }.forEach { builder.addModule(it.moduleName) }
        }.build()

        listener = SplitInstallStateUpdatedListener { state ->
            Log.d(TAG, "State: $state")
            when (state.status()) {
                SplitInstallSessionStatus.DOWNLOADING -> {
                    viewModel.progressIndeterminate.value = false
                    val progressValue = ((state.bytesDownloaded() / state.totalBytesToDownload()) * 100).toInt()
                    Log.i(TAG, "Progress value: $progressValue")
                    viewModel.progress.value = progressValue
                }
                SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                    startIntentSender(state.resolutionIntent()?.intentSender, null, 0, 0, 0)
                }
                SplitInstallSessionStatus.INSTALLED -> {
                    userSp.edit {
                        putInt(SP_KEY_MODULE_INSTALLATION_STATE, STATE_DONE_MODULES_DOWNLOADED)
                        putStringSet(SP_KEY_MODULES_ACTIVE, modulesToActivate.map { it.moduleName }.toSet())
                    }

                    finish()
                }
                SplitInstallSessionStatus.INSTALLING -> {
                    viewModel.progressIndeterminate.value = true
                }
                SplitInstallSessionStatus.FAILED -> {
                    displayErrorDialog("Module Install ${state.errorCode()}")
                }
            }
        }

        HorariumApplication.manager.registerListener(listener)
        HorariumApplication.manager.startInstall(installRequest).addOnFailureListener { ex ->
            if (ex is SplitInstallException) {
                Log.e(TAG, "SplitInstallException code: ${ex.errorCode}")
            }
            displayErrorDialog(ex.localizedMessage)
        }

        runOnUiThread {
            findViewById<Button>(R.id.module_installation_proceed).visibility = View.INVISIBLE
            findViewById<ProgressBar>(R.id.module_installation_progress).visibility = View.VISIBLE
        }
    }

    private fun displayErrorDialog(message: CharSequence) {
        AlertDialog.Builder(this)
                .setMessage(getString(R.string.module_installation_error).format(message))
                .setNegativeButton(R.string.skip) { _, _ ->
                    userSp.edit { putInt(SP_KEY_MODULE_INSTALLATION_STATE, STATE_SKIPPED) }
                    finish()
                }
                .setPositiveButton(R.string.try_again_later) { _, _ ->
                    finish()
                }
                .create()
                .also { runOnUiThread { it.show() } }
    }
}
