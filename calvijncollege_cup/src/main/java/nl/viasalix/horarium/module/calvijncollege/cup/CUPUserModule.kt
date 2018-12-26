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

package nl.viasalix.horarium.module.calvijncollege.cup

import android.content.SharedPreferences
import android.util.Log
import nl.viasalix.horarium.events.UserModuleEventsProvider
import nl.viasalix.horarium.events.args.ContextEventArgs
import nl.viasalix.horarium.events.args.AppointmentsReadyEventArgs
import nl.viasalix.horarium.module.HorariumUserModule
import nl.viasalix.horarium.module.calvijncollege.cup.ui.setup.CalvijncollegeCupSetup

class CUPUserModule : HorariumUserModule() {

    companion object {
        const val TAG: String = "HORARIUM/CC/CUP"
        const val SP_KEY_SETUP_COMPLETED: String = "setupCompleted"
        const val SP_KEY_CONFIG_FIRST_LETTERS_OF_SURNAME = "config_firstLettersOfSurname";
        const val SP_KEY_CONFIG_INTERNAL_USERNAME_IDENTIFIER = "config_internalUsernameIdentifier"
        const val SP_KEY_CONFIG_PIN = "config_pin"
    }

    private lateinit var moduleSp: SharedPreferences

    override fun preSetup(moduleSp: SharedPreferences, eventsProvider: UserModuleEventsProvider) {
        this.moduleSp = moduleSp

        eventsProvider.appointmentsReady += ::appointmentsReady
        eventsProvider.provideMainDrawerMenuItems += ::provideMainDrawerMenuItems
    }

    override fun provideSetupActivityClass(): Class<CalvijncollegeCupSetup>? {
        if (moduleSp.getBoolean(SP_KEY_SETUP_COMPLETED, false)) return null
        return CalvijncollegeCupSetup::class.java
    }

    override fun init() {
        Log.d(TAG, "Initializing CUP module...")

        val cupClient = CUPClient()
        val (initSuccess, initFailReason) = cupClient.init(
            moduleSp.getString(SP_KEY_CONFIG_FIRST_LETTERS_OF_SURNAME, "") ?: "",
            moduleSp.getString(SP_KEY_CONFIG_INTERNAL_USERNAME_IDENTIFIER, "") ?: "",
            moduleSp.getString(SP_KEY_CONFIG_PIN, "") ?: "")
    }

    private fun appointmentsReady(args: AppointmentsReadyEventArgs) {
        Log.d(TAG, "Appointments ready!")
    }

    private fun provideMainDrawerMenuItems(args: ContextEventArgs): Map<String, () -> Unit> {
        return mapOf(
            args.context.getString(R.string.module_calvijncollege_cup_menu_choices) to {
                // TODO: Start a new activity
            }
        )
    }
}