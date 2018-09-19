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

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

class ConcurrentSetEvent<T, U> : Event<T, U>() where T : IEventArgs {

    private val subscribers: MutableSet<(T) -> U> = Collections.newSetFromMap(ConcurrentHashMap())

    override fun subscribe(callback: (T) -> U) = subscribers.add(callback)

    override fun unsubscribe(callback: (T) -> U) = subscribers.remove(callback)

    override fun invoke(args: T): List<U> {
        val returnValues: MutableList<U> = ArrayList(subscribers.size)

        subscribers.forEach {
            returnValues.add(it(args))
        }

        return returnValues.toList()
    }

}