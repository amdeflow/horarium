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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import nl.viasalix.horarium.R
import nl.viasalix.horarium.module.ModuleStatusReport

class ModuleItemAdapter(
    private val moduleStatusReports: Array<ModuleStatusReport>,
    private val strInstalled: String,
    private val strNotInstalled: String
) : RecyclerView.Adapter<ModuleItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleItemViewHolder {
        return ModuleItemViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.module_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ModuleItemViewHolder, position: Int) {
        val report = moduleStatusReports[position]

        holder.title.text = report.displayName
        holder.activationState.isChecked = report.activated
        holder.installationState.text =
            if (report.installed) strInstalled
            else strNotInstalled
    }

    override fun getItemCount(): Int = moduleStatusReports.size
}