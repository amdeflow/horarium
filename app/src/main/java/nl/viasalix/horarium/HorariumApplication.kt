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

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.play.core.splitcompat.SplitCompatApplication
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import nl.viasalix.horarium.module.ModuleManager

class HorariumApplication : SplitCompatApplication() {

    companion object {
        lateinit var manager: SplitInstallManager

        /**
         * Restart the application.
         * Launches the [MainActivity] directly when closed.
         *
         * Note: make sure everything like shared preferences are stored.
         */
        fun restart(context: Context) {
            val mStartActivity = Intent(context, MainActivity::class.java)
            val mPendingIntentId = 123456
            val mPendingIntent = PendingIntent.getActivity(
                context,
                mPendingIntentId,
                mStartActivity,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
            val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
            System.exit(0)
        }
    }

    override fun onCreate() {
        super.onCreate()

        manager = SplitInstallManagerFactory.create(this)

        ModuleManager.loadDefinitions(this)
    }
}