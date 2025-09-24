/*
 * Copyright 2025 Jeff Lockhart
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
package kotbase.ext

import kotbase.Blob

internal val Blob.Companion.TYPE_BLOB get() = "blob"
internal val Blob.Companion.META_PROP_TYPE get() = "@type"
internal val Blob.Companion.PROP_DIGEST get() = "digest"
internal val Blob.Companion.PROP_LENGTH get() = "length"
internal val Blob.Companion.PROP_CONTENT_TYPE get() = "content_type"
