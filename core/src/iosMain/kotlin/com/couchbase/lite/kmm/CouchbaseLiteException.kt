package com.couchbase.lite.kmm

public actual class CouchbaseLiteException
internal constructor(
    message: String?,
    cause: Exception?,
    domain: String?,
    code: Int,
    private val info: Map<String, Any?>?
) : Exception(message, cause) {

    public actual constructor(message: String) : this(message, null, null, 0, null)

    public actual constructor(message: String, cause: Exception) :
            this(message, cause, null, 0, null)

    public actual constructor(message: String, domain: String, code: Int) :
            this(message, null, domain, code, null)

    public actual constructor(message: String, cause: Exception, domain: String, code: Int) :
            this(message, cause, domain, code, null)

    private val code = if (code > 0) code else 10

    private val domain: String = domain ?: CBLError.Domain.CBLITE

    public actual fun getDomain(): String = domain

    public actual fun getCode(): Int = code

    public actual fun getInfo(): Map<String, Any?>? = info

    override fun toString(): String {
        val msg = message
        return "CouchbaseLiteException{$domain,$code,${if (msg == null) "" else "'$msg'"}}"
    }
}
