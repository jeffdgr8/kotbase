package kotbase

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

    private val domain: String =
        if (domain != null) {
            if (domain.startsWith("NS")) {
                domain.substring(2)
            } else {
                domain
            }
        } else {
            CBLError.Domain.CBLITE
        }

    private val code: Int = if (code > 0) code else CBLError.Code.UNEXPECTED_ERROR

    internal actual fun getDomain(): String = domain

    internal actual fun getCode(): Int = code

    internal actual fun getInfo(): Map<String, Any?>? = info

    override fun toString(): String {
        val msg = message
        return "CouchbaseLiteException{$domain,$code,${if (msg == null) "" else "'$msg'"}}"
    }
}
