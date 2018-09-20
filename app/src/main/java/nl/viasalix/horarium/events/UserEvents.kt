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

import nl.viasalix.horarium.events.args.AppointmentsReadyEventArgs
import nl.viasalix.horarium.events.args.RenderAppointmentEventArgs

class UserEvents {
    /**
     * This event is invoked when appointments are loaded. The result is not used, and you should not modify any content
     * of the event args.
     */
    val appointmentsReady = ConcurrentSetEvent<AppointmentsReadyEventArgs, Unit>()

    /**
     * This event is invoked just before the item is being rendered. This method should return as quick as possible,
     * to make the list view as smooth as possible.
     *
     * TODO: Support for custom chips.
     * TODO: Custom styling.
     */
    val renderAppointment = ConcurrentSetEvent<RenderAppointmentEventArgs, Unit>()
}