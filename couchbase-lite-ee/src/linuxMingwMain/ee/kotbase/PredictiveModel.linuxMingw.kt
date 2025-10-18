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
package kotbase

import kotbase.util.to
import kotlinx.cinterop.CValue
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.cValue
import kotlinx.cinterop.staticCFunction
import libcblite.CBLPredictiveModel
import libcblite.FLArray_Retain
import libcblite.FLDict_Retain

internal fun PredictiveModel.convert(): CValue<CBLPredictiveModel> {
    return cValue {
        context = StableRef.create(this@convert).asCPointer()
        prediction = staticCFunction { ref, input ->
            with(ref.to<PredictiveModel>()) {
                val output = predict(Dictionary(input!!, null, release = false))
                // FLDict and its values should not be released by the Dictionary object
                output?.retain()
                output?.actual
            }
        }
        unregistered = staticCFunction { ref ->
            ref?.asStableRef<PredictiveModel>()?.dispose()
        }
    }
}

private fun Dictionary.retain() {
    FLDict_Retain(actual)
    forEach { key ->
        when (val value = getValue(key)) {
            is Array -> value.retain()
            is Dictionary -> value.retain()
        }
    }
}

private fun Array.retain() {
    FLArray_Retain(actual)
    forEach { value ->
        when (value) {
            is Array -> value.retain()
            is Dictionary -> value.retain()
        }
    }
}
