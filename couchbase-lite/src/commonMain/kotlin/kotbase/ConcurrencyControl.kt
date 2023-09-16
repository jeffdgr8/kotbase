/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kotbase

/**
 * ConcurrencyControl type used when saving or deleting a document.
 */
public expect enum class ConcurrencyControl {

    /**
     * The last write operation will win if there is a conflict.
     */
    LAST_WRITE_WINS,

    /**
     * The operation will fail if there is a conflict.
     */
    FAIL_ON_CONFLICT
}
