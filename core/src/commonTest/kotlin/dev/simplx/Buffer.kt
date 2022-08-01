@file:Suppress("MemberVisibilityCanBePrivate")

package dev.simplx

/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
abstract class Buffer internal constructor(capacity: Int) {
    val capacity: Int

    var limit: Int

    var mark = UNSET_MARK

    var position = 0


    fun capacity(): Int {
        return capacity
    }

    /**
     * Indicates if there are elements remaining in this buffer, that is if
     * `position < limit`.
     *
     * @return `true` if there are elements remaining in this buffer,
     * `false` otherwise.
     */
    fun hasRemaining(): Boolean {
        return position < limit
    }

    /**
     * Returns the limit of this buffer.
     *
     * @return the limit of this buffer.
     */
    fun limit(): Int {
        return limit
    }


    fun limit(newLimit: Int): Buffer {
        require(!(newLimit < 0 || newLimit > capacity))
        limit = newLimit
        if (position > newLimit) {
            position = newLimit
        }
        if (mark != UNSET_MARK && mark > newLimit) {
            mark = UNSET_MARK
        }
        return this
    }

    fun position(): Int {
        return position
    }

    fun position(newPosition: Int): Buffer {
        require(!(newPosition < 0 || newPosition > limit))
        position = newPosition
        if (mark != UNSET_MARK && mark > position) {
            mark = UNSET_MARK
        }
        return this
    }

    fun remaining(): Int {
        return limit - position
    }

    fun rewind(): Buffer {
        position = 0
        mark = UNSET_MARK
        return this
    }

    companion object {
        const val UNSET_MARK = -1
    }

    init {
        require(capacity >= 0)
        limit = capacity
        this.capacity = limit
    }
}
