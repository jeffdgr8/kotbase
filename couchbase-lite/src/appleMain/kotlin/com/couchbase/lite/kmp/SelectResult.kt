package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLQuerySelectResult
import com.udobny.kmp.DelegatedClass

public actual open class SelectResult
private constructor(actual: CBLQuerySelectResult) :
    DelegatedClass<CBLQuerySelectResult>(actual) {

    public actual class From
    internal constructor(private val all: (String?) -> CBLQuerySelectResult) :
        SelectResult(all(null)) {

        public actual fun from(alias: String): SelectResult =
            SelectResult(all(alias))
    }

    public actual class As
    private constructor(
        private val function: (Any, String?) -> CBLQuerySelectResult,
        private val param1: Any
    ) : SelectResult(function(param1, null)) {

        internal companion object {

            @Suppress("UNCHECKED_CAST")
            internal operator fun <T : Any> invoke(
                function: (T, String?) -> CBLQuerySelectResult,
                param1: T
            ): As = As(function as (Any, String?) -> CBLQuerySelectResult, param1 as Any)
        }

        public actual fun `as`(alias: String): SelectResult {
            return SelectResult(function(param1, alias))
        }
    }

    public actual companion object {

        public actual fun property(property: String): As =
            As(CBLQuerySelectResult.Companion::property, property)

        public actual fun expression(expression: Expression): As =
            As(CBLQuerySelectResult.Companion::expression, expression.actual)

        public actual fun all(): From =
            From(CBLQuerySelectResult.Companion::allFrom)
    }
}
