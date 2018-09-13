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

import android.util.Log
import com.google.android.play.core.splitcompat.SplitCompatApplication
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import nl.viasalix.horarium.module.ModuleManager

class HorariumApplication : SplitCompatApplication() {

    companion object {
        lateinit var manager: SplitInstallManager
        val listener = SplitInstallStateUpdatedListener { state ->
            val multiInstall = state.moduleNames().size > 1
            state.moduleNames().forEach { name ->
                // Handle changes in state.
                when (state.status()) {
                    SplitInstallSessionStatus.DOWNLOADING -> {
                        Log.i("HORARIUM", "Downloading $name")
                    }
                    SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                        // startIntentSender(state.resolutionIntent()?.intentSender, null, 0, 0, 0)
                    }
                    SplitInstallSessionStatus.INSTALLED -> {
                        Log.i("HORARIUM", "Installed: $name")
                    }

                    SplitInstallSessionStatus.INSTALLING -> Log.i("HORARIUM", "Installing $name")
                    SplitInstallSessionStatus.FAILED -> {
                        Log.i("HORARIUM", "Failed installing $name")
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        manager = SplitInstallManagerFactory.create(this)
        manager.registerListener(listener)

        ModuleManager.loadDefinitions(this)
    }
}