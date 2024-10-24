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
 * **ENTERPRISE EDITION API**
 *
 * PredictiveModel protocol that allows to integrate machine learning model into
 * CouchbaseLite Query via invoking the Function.prediction() function.
 */
public fun interface PredictiveModel {

    /**
     * The prediction callback called when invoking the Function.prediction() function
     * inside a query or an index. The input dictionary object's keys and values will be
     * corresponding to the 'input' dictionary parameter of theFunction.prediction() function.
     * <br></br>
     * If the prediction callback cannot return a result, the prediction callback
     * should return null value, which will be evaluated as MISSING.
     *
     * @param input The input dictionary.
     * @return The output dictionary.
     */
    public fun predict(input: Dictionary): Dictionary?
}
