/*
 * Copyright 2019 Jochem Broekhoff and Rutger Broekhoff
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

package nl.viasalix.horarium.data

import android.view.View

/**
 * When a value is `null`, it is assumed to be not edited.
 */
data class AppointmentCustomizations(
        val subjects: List<String>? = null,
        val teachers: List<String>? = null,
        val locations: List<String>? = null,
        val extraChips: List<ChipStub>? = null
)

data class ChipStub(
        val name: String,
        val action: (View?) -> Unit
)
