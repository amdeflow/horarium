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

package nl.viasalix.horarium.module

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import nl.viasalix.horarium.HorariumApplication
import nl.viasalix.horarium.R
import nl.viasalix.horarium.events.UserModuleEventsProvider
import nl.viasalix.horarium.utils.Constants.SP_KEY_MODULES_ACTIVE
import nl.viasalix.horarium.utils.Constants.SP_KEY_MODULES_PROMPTED
import nl.viasalix.horarium.utils.Constants.SP_KEY_SCHOOL_NAME
import nl.viasalix.horarium.utils.Constants.SP_KEY_USER_IDENTIFIER
import java.io.InputStreamReader
import java.util.UUID

object ModuleManager {

    private const val TAG: String = "Horarium/ModuleManager"
    private val gson: Gson = GsonBuilder().create()

    private var modulesPerInstitute: Map<String, List<String>> = emptyMap()
    private var moduleMetadata: Map<String, ModuleMetadata> = emptyMap()

    private val setupRequests: MutableMap<String, () -> Unit> = HashMap()

    internal fun loadDefinitions(context: Context) {
        context.resources.openRawResource(R.raw.modules_per_institute).use {
            modulesPerInstitute = gson.fromJson(
                    InputStreamReader(it),
                    object : TypeToken<Map<String, List<String>>>() {}.type
            )
        }

        context.resources.openRawResource(R.raw.module_metadata).use {
            moduleMetadata = gson.fromJson(
                    InputStreamReader(it),
                    object : TypeToken<Map<String, ModuleMetadata>>() {}.type
            )
        }
    }

    private fun listInstalledModules(): Set<String> {
        return HorariumApplication.manager.installedModules.intersect(moduleMetadata.keys)
    }

    /**
     * Check if the current user must be prompted for module installation.
     */
    internal fun mustPromptModuleInstallation(userSp: SharedPreferences): Boolean {
        Log.d(TAG, "mustPromptModuleInstallation check")

        val prompted = userSp.getBoolean(SP_KEY_MODULES_PROMPTED, false)

        if (prompted) return false

        return listAvailableModules(userSp).isNotEmpty()
    }

    fun listAvailableModules(userSp: SharedPreferences): List<String> {
        var schoolName = userSp.getString(SP_KEY_SCHOOL_NAME, "")

        if (schoolName == null)
            schoolName = ""

        if (!modulesPerInstitute.containsKey(schoolName)) {
            Log.d(TAG, "modulesPerInstitute does not contain definitions for $schoolName")
            return emptyList()
        }

        return modulesPerInstitute.getValue(schoolName)
    }

    fun listActiveModules(userSp: SharedPreferences): Set<String> {
        return userSp.getStringSet(SP_KEY_MODULES_ACTIVE, emptySet())!!
    }

    fun createStatusReport(availableModules: List<String>, activeModules: List<String>): Array<ModuleStatusReport> {
        if (availableModules.isEmpty()) return emptyArray()

        val installedModules = listInstalledModules()

        return Array(availableModules.size) { i ->
            val module = availableModules[i]
            val moduleMeta = moduleMetadata[module]
            val moduleDescription = moduleMeta?.description ?: module

            return@Array ModuleStatusReport(
                    module,
                    installedModules.contains(module),
                    moduleDescription,
                    activeModules.contains(module)
            )
        }
    }

    internal fun instantiateModulesAndPreSetup(context: Context, userSp: SharedPreferences, eventsProvider: UserModuleEventsProvider): List<HorariumUserModule> {
        Log.d(TAG, "Instantiating active modules and performing pre-setup")

        val initializedModules: MutableList<HorariumUserModule> = mutableListOf()

        listActiveModules(userSp).forEach { moduleName ->
            Log.d(TAG, "Instantiating modules provided by $moduleName")
            val moduleMeta = moduleMetadata[moduleName]

            moduleMeta?.userModules?.forEach { userModuleClassName ->
                val className = "${moduleMeta.`package`}.$userModuleClassName"
                val userIdentifier = userSp.getString(SP_KEY_USER_IDENTIFIER, null)
                Log.d(TAG, "Instantiating module from class $className")

                if (userIdentifier != null) {
                    try {
                        val userModuleInstance =
                                Class.forName(className).asSubclass(HorariumUserModule::class.java).newInstance()

                        userModuleInstance.preSetup(
                                context.getSharedPreferences(
                                        userIdentifier + "_module_$className",
                                        Context.MODE_PRIVATE
                                ),
                                eventsProvider
                        )

                        initializedModules.add(userModuleInstance)
                    } catch (e: Exception) {
                        Log.i(TAG, "Caught exception when instantiating the user module $className", e)
                    }
                }
            }
        }

        return initializedModules
    }

    internal fun requestSetup(callback: () -> Unit): String {
        val id = UUID.randomUUID().toString()
        setupRequests[id] = callback
        return id
    }

    /**
     * Notify the [ModuleManager] that the module setup with the given [id] has finished and is ready to use.
     */
    fun completeSetup(id: String) {
        setupRequests.remove(id)?.invoke()
    }
}
