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

package nl.viasalix.horarium

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.material.bottomappbar.BottomAppBar
import nl.viasalix.horarium.events.UserEvents
import nl.viasalix.horarium.module.HorariumUserModule
import nl.viasalix.horarium.module.ModuleManager
import nl.viasalix.horarium.ui.drawer.BottomDrawer
import nl.viasalix.horarium.ui.main.ScheduleFragment
import nl.viasalix.horarium.utils.*
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.doAsync

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "Horarium/Main"
    }

    private lateinit var userSp: SharedPreferences
    private val userEvents = UserEvents()
    private var moduleInstances: List<HorariumUserModule> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        val currentUser = defaultSharedPreferences.getString(SP_KEY_CURRENT_USER, null)

        if (!defaultSharedPreferences.contains(SP_KEY_USERS) || currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))

            finish()
            super.onCreate(savedInstanceState)
            return
        }

        userSp = getSharedPreferences(currentUser, Context.MODE_PRIVATE)

        Log.d(TAG, "Checking modules state...")

        var installationPrompted = false
        if (ModuleManager.mustPromptModuleInstallation(this, userSp)) {
            val availableModules = ModuleManager.listAvailableModules(this, userSp)
            val activeModules = ModuleManager.listActiveModules(this, userSp)

            startActivity(
                Intent(this, ModuleInstallationActivity::class.java)
                    .putStringArrayListExtra("availableModules", ArrayList(availableModules))
                    .putStringArrayListExtra("activeModules", ArrayList(activeModules))
                    .putExtra("userSpName", currentUser)
            )

            installationPrompted = true
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            // TODO: Add userEvents back
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ScheduleFragment())
                .commitNow()
        }

        setSupportActionBar(findViewById(R.id.bottomAppBar))

        // Skip initialization when the installation was just prompted to the user
        if (!installationPrompted) {
            initializeModuleAsync()
        }

        findViewById<BottomAppBar>(R.id.bottomAppBar).setNavigationOnClickListener { _ ->
            BottomDrawer().showNow(supportFragmentManager, "bottom_drawer")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onResume() {
        super.onResume()

        val installationState = userSp.getInt(SP_KEY_MODULE_INSTALLATION_STATE, -1)

        if (installationState > -1)
            userSp.edit(commit = true) {
                putBoolean(SP_KEY_MODULES_PROMPTED, true)
                putInt(SP_KEY_MODULE_INSTALLATION_STATE, -1)
            }

        when (installationState) {
            ModuleInstallationActivity.STATE_SKIPPED -> {
                // Skipped
            }
            ModuleInstallationActivity.STATE_DONE_NOTHING_DOWNLOADED -> {
                initializeModuleAsync()
            }
            ModuleInstallationActivity.STATE_DONE_MODULES_DOWNLOADED -> {
                /*
                 * "When downloading dynamic feature modules on-demand, devices running Android 6.0 (API level 23) and
                 * lower require the app to restart before completing installation of the new modules", as stated in the
                 * docs: https://developer.android.com/studio/projects/dynamic-delivery
                 */
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    HorariumApplication.restart(this)
                } else {
                    initializeModuleAsync()
                }
            }
        }
    }

    private fun initializeModuleAsync() {
        doAsync {
            val act = weakRef.get()
            if (act != null) {
                val initializedModules = ModuleManager.initializeModules(act, userSp, userEvents)
                setupNext(initializedModules.iterator())
            }
        }
    }

    private fun setupNext(iterator: Iterator<HorariumUserModule>) {
        if (!iterator.hasNext()) return

        val next = iterator.next()
        val activityClass = next.provideSetupActivityClass()

        if (activityClass == null) {
            setupNext(iterator)
            return
        }

        val finishKey = ModuleManager.requestSetup {
            setupNext(iterator)
        }

        val userIdentifier = userSp.getString(SP_KEY_USER_IDENTIFIER, "")!!
        val moduleSpKey = userIdentifier + "_module_" + next.javaClass.name

        Intent(this, activityClass)
            .putExtra("moduleSharedPreferencesKey", moduleSpKey)
            .putExtra("setupCompleteId", finishKey)
            .also {
                startActivity(it)
            }
    }
}
