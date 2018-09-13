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

package nl.viasalix.horarium.module.calvijncollege.cup.data

import java.util.Date

/**
 * @param start Start date.
 * @param end End date, can be null.
 * @param slot Slot for timetable.
 * @param selectedOption Selected [Option], can be null.
 * @param fixed `true` if this appointment is fixed. This probably means that no choices are available.
 * @param choices List of available [Choice]s.
 */
data class Appointment(
    val start: Date,
    val end: Date?,
    val slot: Int,
    val selectedOption: Option?,
    val fixed: Boolean,
    val choices: List<Choice>
)