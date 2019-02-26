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

package nl.viasalix.horarium.module.calvijncollege.cup.ui.setup

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import nl.viasalix.horarium.module.ModuleManager
import nl.viasalix.horarium.module.calvijncollege.cup.CUPClient
import nl.viasalix.horarium.module.calvijncollege.cup.CUPUserModule
import nl.viasalix.horarium.module.calvijncollege.cup.R
import nl.viasalix.horarium.module.calvijncollege.cup.method.SearchUsers
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk27.coroutines.onClick

class CalvijnCollegeCUPSetup : AppCompatActivity() {

    companion object {
        const val TAG: String = "HOR/CC/Setup"
    }

    var firstLettersOfSurname = ""
    var availableUsers: Map<String, String> = emptyMap()
    var selectedUser = ""
    var pin = ""

    var mNextHandler: (() -> Unit)? = null

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

        Log.i(TAG, "ModuleSpKey=$moduleSpKey, SetupCompleteId=$setupCompleteId")

        moduleSp = getSharedPreferences(moduleSpKey, Context.MODE_PRIVATE)

        findViewById<Button>(R.id.module_calvijncollege_cup_setup_next).onClick {
            doAsync { next() }
        }

        doAsync {
            replaceDetail(SetupStep1())
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.run {
            putString("setupCompleteId", setupCompleteId)
            putString("moduleSharedPreferencesKey", moduleSpKey)
        }
        super.onSaveInstanceState(outState)
    }

    private fun replaceDetail(newFragment: SetupFragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.run {
            newFragment.onDoneCallback = { doAsync { next() } }
            replace(R.id.module_calvijncollege_cup_setup_detailContainer, newFragment)
            disallowAddToBackStack()
            commit()
        }
    }

    /**
     * Will be called from the background thread (by [doAsync]).
     */
    private fun next() {
        if (loading) return
        loading = true

        Log.d(TAG, "Pressed NEXT. Current step = $step")

        replaceDetail(LoadingFragment())

        // Invoke the next handler
        mNextHandler?.invoke()

        when (step) {
            1 -> { // User has entered the first letters of their surname
                val cupClient = CUPClient()
                cupClient.init()

                val searchResult = SearchUsers.execute(cupClient, firstLettersOfSurname)
                if (searchResult.success && searchResult.result.isNotEmpty()) {
                    step = 2
                    availableUsers = searchResult.result

                    Log.d(TAG, "Result of SearchUsers:")
                    for ((identifier, value) in availableUsers) {
                        Log.d(TAG, "$identifier -> $value")
                    }

                    replaceDetail(SetupStep2())
                } else {
                    if (searchResult.success && searchResult.result.isEmpty()) {
                        Log.e(TAG, "Step 1 failed: No users found for the given first letters of the surname.")
                    } else {
                        val failReason = searchResult.failReason
                        if (failReason.startsWith("E_SearchUsers_Plain_")) {
                            val plainFailReason = failReason.substring(20)
                            // TODO: Display plain error
                            Log.e(TAG, "Step 1 failed (plain error): $plainFailReason")
                        } else {
                            // TODO: Display error
                            Log.e(TAG, "Step 1 failed: ${searchResult.failReason}")
                        }
                    }

                    // Reload step 1
                    replaceDetail(SetupStep1())
                }
            }
            2 -> { // User has selected the a user
                // No additional checks have to be performed

                step = 3
                replaceDetail(SetupStep3())
            }
            3 -> { // User has entered the pin code
                Log.d(TAG, "firstLettersOfSurname = $firstLettersOfSurname, selectedUser = $selectedUser, pin = $pin")

                val cupClient = CUPClient()
                val (initSuccess, initFailReason) = cupClient.init(firstLettersOfSurname, selectedUser, pin)

                if (initSuccess) {
                    done()
                } else {
                    if (initFailReason.startsWith("E_SignIn_Plain_")) {
                        val plainFailReason = initFailReason.substring(15)
                        // TODO: Display plain error
                        Log.e(TAG, "Step 3 failed (plain error): $plainFailReason")
                    } else {
                        // TODO: Display error message
                        Log.e(TAG, "Step 3 failed: $initFailReason")
                    }

                    // Reload step 3
                    replaceDetail(SetupStep3())
                }
            }
        }

        loading = false
    }

    /**
     * Store the configured [selectedUser] and [pin] into the module storage and mark the setup as completed.
     * This also notifies the [ModuleManager] that that the setup is complete.
     */
    private fun done() {
        moduleSp.edit(commit = true) {
            putString(CUPUserModule.SP_KEY_CONFIG_FIRST_LETTERS_OF_SURNAME, firstLettersOfSurname)
            putString(CUPUserModule.SP_KEY_CONFIG_INTERNAL_USERNAME_IDENTIFIER, selectedUser)
            putString(CUPUserModule.SP_KEY_CONFIG_PIN, pin)
            putBoolean(CUPUserModule.SP_KEY_SETUP_COMPLETED, true)
        }

        ModuleManager.completeSetup(setupCompleteId)

        runOnUiThread { finish() }
    }

    fun setNextHandler(handler: () -> Unit) {
        mNextHandler = handler
    }
}
