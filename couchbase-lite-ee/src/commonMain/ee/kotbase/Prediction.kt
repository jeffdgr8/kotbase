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
 * The prediction model manager for registering and unregistering predictive models.
 */
public expect object Prediction {

    /**
     * Register a predictive model by the given name.
     *
     * @param name  The name of the predictive model.
     * @param model The predictive model.
     */
    public fun registerModel(name: String, model: PredictiveModel)

    /**
     * Unregister the predictive model of the given name.
     *
     * @param name The name of the predictive model.
     */
    public fun unregisterModel(name: String)
}
