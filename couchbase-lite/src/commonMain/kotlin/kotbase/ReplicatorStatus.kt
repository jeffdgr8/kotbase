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
 * The activity level and progress of a replicator.
 */
public expect class ReplicatorStatus {

    /**
     * The current activity level.
     */
    public val activityLevel: ReplicatorActivityLevel

    /**
     * The current progress of the replicator.
     */
    public val progress: ReplicatorProgress

    /**
     * The current error if there is an error occurred.
     */
    public val error: CouchbaseLiteException?
}
