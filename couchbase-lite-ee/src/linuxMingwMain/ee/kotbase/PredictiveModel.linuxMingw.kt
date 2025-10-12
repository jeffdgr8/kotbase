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
import libcblite.FLDict_Retain

internal fun PredictiveModel.convert(): CValue<CBLPredictiveModel> {
    return cValue {
        context = StableRef.create(PredictiveModelHolder(this@convert)).asCPointer()
        prediction = staticCFunction { ref, input ->
            with(ref.to<PredictiveModelHolder>()) {
                model.predict(Dictionary(input!!, null, retain = false))?.actual.also {
                    // FLDict should not be released by the Dictionary object
                    FLDict_Retain(it)
                }
            }
        }
        unregistered = staticCFunction { ref ->
            ref?.asStableRef<PredictiveModelHolder>()?.dispose()
        }
    }
}

internal class PredictiveModelHolder(val model: PredictiveModel)
