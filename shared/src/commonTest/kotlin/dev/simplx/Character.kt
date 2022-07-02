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
object Character {

    /**
     * Indicates whether the specified character is a digit.
     *
     * @param c
     * the character to check.
     * @return `true` if `c` is a digit; `false`
     * otherwise.
     */
    fun isDigit(c: Char): Boolean = c in '0'..'9'

    /**
     * Indicates whether the specified character is an upper case letter.
     *
     * @param c
     * the character to check.
     * @return `true` if `c` is a upper case letter; `false`
     * otherwise.
     */
    fun isUpperCase(c: Char): Boolean = c in 'A'..'Z'
}