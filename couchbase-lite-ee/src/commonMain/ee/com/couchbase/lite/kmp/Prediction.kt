package com.couchbase.lite.kmp

/**
 * **ENTERPRISE EDITION API**
 *
 * The prediction model manager for registering and unregistering predictive models.
 */
public expect class Prediction {

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
