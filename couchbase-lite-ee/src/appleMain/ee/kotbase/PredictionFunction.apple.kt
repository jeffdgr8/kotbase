package kotbase

import cocoapods.CouchbaseLite.CBLQueryPredictionFunction

public actual class PredictionFunction(
    internal val actual: CBLQueryPredictionFunction
) : Expression(actual) {

    public actual fun propertyPath(path: String): Expression =
        Expression(actual.property(path))
}

// TODO: casting the existing property fails with:
// Undefined symbols for architecture x86_64:
//   "_OBJC_CLASS_$_CBLQueryPredictionFunction", referenced from:
//       objc-class-ref in test.kexe.o
// ld: symbol(s) not found for architecture x86_64
//internal val PredictionFunction.actual: CBLQueryPredictionFunction
//    get() = platformState.actual as CBLQueryPredictionFunction
