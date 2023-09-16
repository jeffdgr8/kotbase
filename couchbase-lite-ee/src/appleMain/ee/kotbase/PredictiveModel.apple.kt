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

import cocoapods.CouchbaseLite.CBLDictionary
import cocoapods.CouchbaseLite.CBLPredictiveModelProtocol
import platform.darwin.NSObject

internal fun PredictiveModel.convert(): CBLPredictiveModelProtocol {
    return object : NSObject(), CBLPredictiveModelProtocol {

        override fun predict(input: CBLDictionary): CBLDictionary? =
            predict(Dictionary(input))?.actual
    }
}
