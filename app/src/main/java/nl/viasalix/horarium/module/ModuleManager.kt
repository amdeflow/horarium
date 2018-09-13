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
import nl.viasalix.horarium.R
import java.io.InputStreamReader

object ModuleManager {

    private const val TAG: String = "HOR/ModuleManager"
    private val gson: Gson = GsonBuilder().create()

    private var modulesPerInstitute: Map<String, List<String>> = emptyMap()
    private var moduleMetadata: Map<String, ModuleMetadata> = emptyMap()

    fun loadDefinitions(context: Context) {
        context.resources.openRawResource(R.raw.modules_per_institute).use {
            modulesPerInstitute =
                gson.fromJson(InputStreamReader(it), object : TypeToken<Map<String, List<String>>>() {}.type)
        }

        context.resources.openRawResource(R.raw.module_metadata).use {
            moduleMetadata =
                gson.fromJson(InputStreamReader(it), object : TypeToken<Map<String, ModuleMetadata>>() {}.type)
        }
    }

    /**
     * Check if the current user must be prompted for module installation.
     */
    fun mustPromptModuleInstallation(context: Context, userSp: SharedPreferences): Boolean {
        Log.d(TAG, "mustPromptModuleInstallation check.")

        val prompted = userSp.getBoolean(context.getString(R.string.SP_KEY_MODULES_PROMPTED), false)

        if (prompted) return false

        return listAvailableModules(context, userSp).isNotEmpty()
    }

    fun listAvailableModules(context: Context, userSp: SharedPreferences): List<String> {
        var schoolName = userSp.getString(context.getString(R.string.SP_KEY_SCHOOL_NAME), "")

        if (schoolName == null)
            schoolName = ""

        if (!modulesPerInstitute.containsKey(schoolName)) {
            Log.d(TAG, "ModulesPerInstitute does not contain definitions for $schoolName.")
            return emptyList()
        }

        return modulesPerInstitute[schoolName]!!
    }

    fun listActiveModules(context: Context, userSp: SharedPreferences): Set<String> {
        return userSp.getStringSet(context.getString(R.string.SP_KEY_MODULES_ACTIVE), emptySet())!!
    }

    fun initializeModules(context: Context, userSp: SharedPreferences): Set<HorariumUserModule> {
        Log.d(TAG, "Initializing active modules...")

        val set: MutableSet<HorariumUserModule> = HashSet()

        listActiveModules(context, userSp).forEach { moduleName ->
            Log.d(TAG, "Initializing modules provided by $moduleName...")
            val moduleMeta = moduleMetadata[moduleName]

            moduleMeta?.userModules?.forEach { userModuleClassName ->
                val className = "${moduleMeta.`package`}.$userModuleClassName"
                val userIdentifier = userSp.getString(context.getString(R.string.SP_KEY_USER_IDENTIFIER), null)
                Log.d(TAG, "Initializing module from class $className")

                if (userIdentifier != null) {
                    try {
                        val userModuleInstance =
                            Class.forName(className).asSubclass(HorariumUserModule::class.java).newInstance()
                        userModuleInstance.init(
                            context.getSharedPreferences(
                                userIdentifier + "_module_$className",
                                Context.MODE_PRIVATE
                            )
                        )
                        set.add(userModuleInstance)
                    } catch (e: Exception) {
                        Log.i(TAG, "Caught exception when instantiating the user module $className", e)
                    }
                }
            }
        }

        return set
    }
}