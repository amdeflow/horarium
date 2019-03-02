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
import androidx.room.RoomDatabase
import nl.viasalix.horarium.data.AppointmentCustomizations
import nl.viasalix.horarium.data.zermelo.model.Appointment
import nl.viasalix.horarium.events.UserModuleEventsProvider
import nl.viasalix.horarium.events.args.ContextEventArgs
import nl.viasalix.horarium.events.args.AppointmentsReadyEventArgs
import nl.viasalix.horarium.module.HorariumUserModule
import nl.viasalix.horarium.module.calvijncollege.cup.data.persistence.CUPDatabase
import nl.viasalix.horarium.module.calvijncollege.cup.method.PrintableTimetable
import nl.viasalix.horarium.module.calvijncollege.cup.ui.setup.CalvijnCollegeCUPSetup
import nl.viasalix.horarium.utils.DateUtils
import org.jetbrains.anko.doAsync
import java.util.HashSet

class CUPUserModule : HorariumUserModule() {

    companion object {
        const val TAG: String = "HOR/CC/CUP"
        const val SP_KEY_SETUP_COMPLETED: String = "setupCompleted"
        const val SP_KEY_CONFIG_FIRST_LETTERS_OF_SURNAME = "config_firstLettersOfSurname"
        const val SP_KEY_CONFIG_INTERNAL_USERNAME_IDENTIFIER = "config_internalUsernameIdentifier"
        const val SP_KEY_CONFIG_PIN = "config_pin"
    }

    private var lastProcessingTimestamp = 0L
    @Volatile
    private var busy = false
    private val processQueue: MutableSet<Appointment> = HashSet()

    private lateinit var moduleSP: SharedPreferences
    private lateinit var moduleDB: CUPDatabase
    private var cupClient: CUPClient? = null

    override fun preSetup(
            moduleSP: SharedPreferences,
            moduleDB: RoomDatabase?,
            eventsProvider: UserModuleEventsProvider
    ) {
        this.moduleSP = moduleSP
        this.moduleDB = moduleDB as CUPDatabase

        eventsProvider.appointmentsReady += ::appointmentsReady
        eventsProvider.provideMainDrawerMenuItems += ::provideMainDrawerMenuItems
    }

    override fun provideSetupActivityClass(): Class<CalvijnCollegeCUPSetup>? {
        if (moduleSP.getBoolean(SP_KEY_SETUP_COMPLETED, false)) return null
        return CalvijnCollegeCUPSetup::class.java
    }

    override fun provideDatabaseClass() = CUPDatabase::class.java

    override fun init() {
        Log.d(TAG, "Initializing CUP module...")

        doAsync { initCUPClient() }
    }

    private fun initCUPClient() {
        val newCUPClient = CUPClient()
        val (initSuccess, initFailReason) = newCUPClient.init(
                moduleSP.getString(SP_KEY_CONFIG_FIRST_LETTERS_OF_SURNAME, "")!!,
                moduleSP.getString(SP_KEY_CONFIG_INTERNAL_USERNAME_IDENTIFIER, "")!!,
                moduleSP.getString(SP_KEY_CONFIG_PIN, "")!!)

        if (initSuccess) {
            cupClient = newCUPClient
            Log.i(TAG, "Successfully initialized the CUP client.")
        } else {
            Log.e(TAG, "Failed initializing the CUP Client: $initFailReason")
        }
    }

    private fun appointmentsReady(args: AppointmentsReadyEventArgs) {
        Log.d(TAG, "Appointments ready!")

        val eligible = args.appointments.filter { it.subjects.size == 1 && it.subjects[0] == "z_uur" }

        Log.d(TAG, "Eligible size: ${eligible.size}")

        synchronized(processQueue) {
            val currentTimestamp = System.currentTimeMillis()
            if (lastProcessingTimestamp + 1000 < currentTimestamp) {
                processQueue.addAll(eligible)
                lastProcessingTimestamp = currentTimestamp
            }
        }

        if (!busy) {
            busy = true

            Log.d(TAG, "Running background processor.")

            if (cupClient == null) {
                busy = false
                return
            }

            if (!cupClient!!.checkSession(dontUpdateLastRequestTimestamp = true)) {
                initCUPClient()
            }

            val printableTimetableExecution = PrintableTimetable.execute(cupClient!!)
            if (!printableTimetableExecution.success) {
                Log.e(TAG, "Failed executing PrintableTimetable: ${printableTimetableExecution.failReason}")
                busy = false
                return
            }

            val result = printableTimetableExecution.result

            synchronized(processQueue) {
                for (appointment in processQueue) {
                    Log.d(TAG, appointment.toString())

                    val week = DateUtils.getWeek(appointment.start)
                    if (!result.containsKey(week)) continue

                    val narrowedDown = result.getValue(week).filter { it.slot == appointment.startTimeSlot && DateUtils.isSameDay(it.day, appointment.start) }
                    if (narrowedDown.size != 1) {
                        Log.w(TAG, "Size of narrowedDown is NOT equal to 1, instead it has ${narrowedDown.size} items.")
                        continue
                    }

                    val historyOption = narrowedDown[0]
                    val customizations = AppointmentCustomizations(
                            listOf(historyOption.option.subject),
                            listOf(historyOption.option.teacher),
                            listOf(historyOption.option.room)
                    )

                    args.updateAppointmentCustomDataCallback(appointment.appointmentInstance, customizations)
                }

                processQueue.clear()
            }

            busy = false
        }
    }

    private fun provideMainDrawerMenuItems(args: ContextEventArgs): Map<String, () -> Unit> {
        return mapOf(
                args.context.getString(R.string.menu_choices) to {
                    // TODO: Start a new activity
                }
        )
    }
}
