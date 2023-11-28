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
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.runtime.GC
import kotlin.native.runtime.NativeRuntimeApi

actual class PlatformUtilsDelegate : PlatformUtils.Delegate {

    @OptIn(NativeRuntimeApi::class)
    actual override fun gc() {
        GC.collect()
    }

    @OptIn(ExperimentalNativeApi::class)
    actual override fun getAsset(asset: String): Source? {
        val target = when (Platform.osFamily) {
            OsFamily.LINUX -> "linux"
            OsFamily.WINDOWS -> "mingw"
            else -> error("Unsupported platform: ${Platform.osFamily}")
        } + Platform.cpuArchitecture.name.lowercase().replaceFirstChar(Char::titlecase)
        val path = Path("build/bin/$target/debugTest/$asset")
        return SystemFileSystem.source(path).buffered()
    }
}
