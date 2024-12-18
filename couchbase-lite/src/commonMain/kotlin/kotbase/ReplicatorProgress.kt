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
 * Progress of a replicator. If `total` is zero, the progress is indeterminate; otherwise,
 * dividing the two will produce a fraction that can be used to draw a progress bar.
 * The quotient is highly volatile and may be slightly inaccurate by the time it is returned.
 */
public expect class ReplicatorProgress {

    /**
     * The number of completed changes processed.
     */
    public val completed: Long

    /**
     * The total number of changes to be processed.
     */
    public val total: Long
}
