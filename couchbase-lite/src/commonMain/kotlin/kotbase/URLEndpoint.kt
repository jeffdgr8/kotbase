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
 * URL based replication target endpoint
 */
public expect class URLEndpoint

/**
 * Constructor with the url. The supported URL schemes
 * are ws and wss for transferring data over a secure channel.
 *
 * @param url The url.
 */
constructor(url: String) : Endpoint {

    /**
     * Returns the url.
     */
    public val url: String
}
