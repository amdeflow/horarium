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

import android.content.SharedPreferences

open class HorariumUserModule {
    /**
     * Perform installation tasks.
     * The function [done] must be called when the installation is complete.
     *
     * Called only once.
     */
    open fun install(institute: String, done: (Boolean) -> Unit) {}

    /**
     * Initialize the module, performed upon module instantiation.
     * Before this method is called, the user storage container will be ready.
     *
     * Called every time the user session loads.
     *
     * Note: this function will be called from a background thread. UI operations are not safe (and
     * not recommended in the [init] method anyway).
     *
     * @param moduleSp [SharedPreferences] container for this module to store settings.
     */
    open fun init(moduleSp: SharedPreferences) {}

    /**
     * Stop the module gracefully. Must not reset anything.
     *
     * Called when the user switches to a different account.
     */
    open fun stop() {}

    /**
     * Perform a cleanup. Called when an account is removed (e.g. upon sign out).
     */
    open fun cleanup() {}
}