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

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered

actual class PlatformUtilsDelegate : PlatformUtils.Delegate {

    actual override fun gc() {
        System.gc()
    }

    actual override fun getAsset(asset: String): Source? =
        getApplicationContext<Context>().assets.open(asset).asSource().buffered()
}
