package kotbase

import cocoapods.CouchbaseLite.CBLMessageEndpointListener
import kotbase.base.DelegatedClass
import kotbase.ext.asDispatchQueue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

public actual class MessageEndpointListener
internal constructor(actual: CBLMessageEndpointListener) :
    DelegatedClass<CBLMessageEndpointListener>(actual) {

    public actual constructor(config: MessageEndpointListenerConfiguration) : this(
        CBLMessageEndpointListener(config.actual)
    )

    public actual fun accept(connection: MessageEndpointConnection) {
        actual.accept(connection.convert())
    }

    public actual fun close(connection: MessageEndpointConnection) {
        actual.close(connection.convert())
    }

    public actual fun closeAll() {
        actual.closeAll()
    }

    public actual fun addChangeListener(listener: MessageEndpointListenerChangeListener): ListenerToken =
        DelegatedListenerToken(actual.addChangeListener(listener.convert()))

    @OptIn(ExperimentalStdlibApi::class)
    public actual fun addChangeListener(
        context: CoroutineContext,
        listener: MessageEndpointListenerChangeSuspendListener
    ): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val token = actual.addChangeListenerWithQueue(
            context[CoroutineDispatcher]?.asDispatchQueue(),
            listener.convert(scope)
        )
        return SuspendListenerToken(scope, DelegatedListenerToken(token))
    }

    @OptIn(ExperimentalStdlibApi::class)
    public actual fun addChangeListener(scope: CoroutineScope, listener: MessageEndpointListenerChangeSuspendListener) {
        val token = actual.addChangeListenerWithQueue(
            scope.coroutineContext[CoroutineDispatcher]?.asDispatchQueue(),
            listener.convert(scope)
        )
        scope.coroutineContext[Job]?.invokeOnCompletion {
            actual.removeChangeListenerWithToken(token)
        }
    }

    public actual fun removeChangeListener(token: ListenerToken) {
        if (token is SuspendListenerToken) {
            actual.removeChangeListenerWithToken(token.token.actual)
            token.scope.cancel()
        } else {
            token as DelegatedListenerToken
            actual.removeChangeListenerWithToken(token.actual)
        }
    }
}
