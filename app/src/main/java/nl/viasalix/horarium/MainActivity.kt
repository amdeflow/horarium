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
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomappbar.BottomAppBar
import nl.viasalix.horarium.events.UserEvents
import nl.viasalix.horarium.module.ModuleManager
import nl.viasalix.horarium.ui.drawer.BottomDrawer
import nl.viasalix.horarium.ui.main.ScheduleFragment
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.doAsync

class MainActivity : AppCompatActivity() {

    private lateinit var userSp: SharedPreferences
    private val userEvents = UserEvents()

    override fun onCreate(savedInstanceState: Bundle?) {
        val currentUser = defaultSharedPreferences.getString(getString(R.string.SP_KEY_CURRENT_USER), null)

        if (!defaultSharedPreferences.contains(getString(R.string.SP_KEY_USERS)) || currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))

            finish()
            super.onCreate(savedInstanceState)
            return
        }

        userSp = getSharedPreferences(currentUser, Context.MODE_PRIVATE)

        Log.d("HOR", "Checking modules state...")
        if (ModuleManager.mustPromptModuleInstallation(this, userSp)) {
            val availableModules = ModuleManager.listAvailableModules(this, userSp)
            val activeModules = ModuleManager.listActiveModules(this, userSp)

            startActivity(Intent(this, ModuleInstallationActivity::class.java).also {
                it.putStringArrayListExtra("availableModules", ArrayList(availableModules))
                it.putStringArrayListExtra("activeModules", ArrayList(activeModules))
            })

            finish()
            super.onCreate(savedInstanceState)
            return
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ScheduleFragment.newInstance(userEvents))
                .commitNow()
        }

        setSupportActionBar(findViewById(R.id.bottomAppBar))

        doAsync {
            val act = this.weakRef.get()
            if (act != null)
                ModuleManager.initializeModules(act, userSp, userEvents)
        }

        findViewById<BottomAppBar>(R.id.bottomAppBar).setNavigationOnClickListener { _ ->
            BottomDrawer().showNow(supportFragmentManager, "bottom_drawer")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
