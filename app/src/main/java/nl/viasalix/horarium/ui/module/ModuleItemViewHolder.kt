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

package nl.viasalix.horarium.ui.module

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import nl.viasalix.horarium.R

class ModuleItemViewHolder(moduleItem: View) : RecyclerView.ViewHolder(moduleItem) {
    val icon: ImageView = moduleItem.findViewById(R.id.module_item_icon)
    val title: TextView = moduleItem.findViewById(R.id.module_item_title)
    val installationState: TextView = moduleItem.findViewById(R.id.module_item_installationState)
    val activationState: CheckBox = moduleItem.findViewById(R.id.module_item_activationState)
}