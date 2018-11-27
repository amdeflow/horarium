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

package nl.viasalix.horarium.module.calvijncollege.cup.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.core.content.edit
import nl.viasalix.horarium.module.ModuleManager
import nl.viasalix.horarium.module.calvijncollege.cup.CUPClient
import nl.viasalix.horarium.module.calvijncollege.cup.CUPUserModule
import nl.viasalix.horarium.module.calvijncollege.cup.R
import nl.viasalix.horarium.module.calvijncollege.cup.method.SearchUsers
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick

class CalvijncollegeCupSetup : AppCompatActivity() {

    var firstLettersOfSurname = ""
    var selectedUser = ""
    var pin = ""

    lateinit var moduleSpKey: String
    lateinit var setupCompleteId: String
    lateinit var moduleSp: SharedPreferences

    @Volatile
    private var loading = false
    private var step = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calvijncollege_cup_setup)

        if (intent != null) {
            moduleSpKey = intent.getStringExtra("moduleSharedPreferencesKey")
            setupCompleteId = intent.getStringExtra("setupCompleteId")
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("moduleSharedPreferencesKey")) {
                moduleSpKey = savedInstanceState.getString("moduleSharedPreferencesKey")!!
            }

            if (savedInstanceState.containsKey("setupCompleteId")) {
                setupCompleteId = savedInstanceState.getString("setupCompleteId")!!
            }
        }

        Log.i("HOR/CC/Setup", "ModuleSpKey=$moduleSpKey, SetupCompleteId=$setupCompleteId")

        moduleSp = getSharedPreferences(moduleSpKey, Context.MODE_PRIVATE)

        findViewById<Button>(R.id.module_calvijncollege_cup_setup_next).onClick {
            doAsync { next() }
        }

        doAsync {
            weakRef.get()
            val transaction = supportFragmentManager.beginTransaction()
            val fragmentStep1 = SetupStep1()
            transaction.run {
                replace(R.id.module_calvijncollege_cup_setup_detailContainer, fragmentStep1)
                addToBackStack(null) // TODO: Support back button press in the future
                commit()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.run {
            putString("setupCompleteId", setupCompleteId)
            putString("moduleSharedPreferencesKey", moduleSpKey)
        }
        super.onSaveInstanceState(outState)
    }

    /**
     * Will be called from the background thread (by [doAsync]).
     */
    fun next() {
        if (loading) return
        loading = true

        val loadingTransaction = supportFragmentManager.beginTransaction()
        val loadingProgress = LoadingFragment()
        loadingTransaction.run {
            replace(R.id.module_calvijncollege_cup_setup_detailContainer, loadingProgress)
            addToBackStack(null)
            commit()
        }

        when (step) {
            1 -> { // User has entered the first letters of their surname
                val cupClient = CUPClient()
                cupClient.init()

                val searchResult = SearchUsers.execute(cupClient, "bro")
                if (searchResult.success) {
                    step = 2
                } else {

                }
            }
            2 -> { // User has selected the a user
                step = 3
                // TODO: Load fragment SetupStep3
            }
            3 -> { // User has entered the pin code
                val cupClient = CUPClient()
                var initResult = cupClient.init("bro", "1", "1275")

                done()
            }
        }

        loading = false
    }

    fun done () {
        moduleSp.edit(commit = true) {
            putBoolean(CUPUserModule.SP_KEY_SETUP_COMPLETED, true)
        }

        ModuleManager.completeSetup(setupCompleteId)

        runOnUiThread { finish() }
    }
}
