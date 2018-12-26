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

package nl.viasalix.horarium.events

import nl.viasalix.horarium.events.args.ContextEventArgs
import nl.viasalix.horarium.events.args.AppointmentsReadyEventArgs
import nl.viasalix.horarium.events.args.EmptyEventArgs

class UserModuleEventsProvider {

    val refresh = ConcurrentSetEvent<EmptyEventArgs, Unit>()

    val appointmentsReady = ConcurrentSetEvent<AppointmentsReadyEventArgs, Unit>()

    /**
     * This event is invoked when the main drawer menu is created. This methoud should return as quick as possible, to
     * make the menu appear as smooth as possible.
     *
     * When the event is invoked, it should return a pair of a string and a lambda function. The string is the text that
     * is displayed in the menu. The lambda function is the tap callback.
     *
     * Please note that the callback is called from the UI thread.
     */
    val provideMainDrawerMenuItems = ConcurrentSetEvent<ContextEventArgs, Map<String, () -> Unit>>()
}