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
