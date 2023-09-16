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

import java.net.URI
import com.couchbase.lite.URLEndpoint as CBLURLEndpoint

public actual class URLEndpoint
internal constructor(actual: CBLURLEndpoint) : Endpoint(actual) {

    public actual constructor(url: String) : this(CBLURLEndpoint(URI(url)))

    public actual val url: String
        get() = actual.url.toString()
}

internal val URLEndpoint.actual: CBLURLEndpoint
    get() = platformState.actual as CBLURLEndpoint
