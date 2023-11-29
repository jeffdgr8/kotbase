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
package kotbase.internal.utils

import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.Foundation.NSBundle
import kotlin.native.runtime.GC
import kotlin.native.runtime.NativeRuntimeApi

actual class PlatformUtilsDelegate : PlatformUtils.Delegate {

    @OptIn(NativeRuntimeApi::class)
    actual override fun gc() {
        GC.collect()
    }

    actual override fun getAsset(asset: String): Source? {
        val dotIndex = asset.lastIndexOf('.')
        val name = asset.substring(0, dotIndex)
        val type = asset.substring(dotIndex + 1)
        val path = NSBundle.mainBundle
            .pathForResource(name, type)
            ?: return null
        return SystemFileSystem.source(Path(path)).buffered()
    }
}
