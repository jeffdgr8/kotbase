package kotbase

/**
 * A change listener protocol.
 */
public typealias MessageEndpointListenerChangeListener = ChangeListener<MessageEndpointListenerChange>

/**
 * A change listener protocol, called within a coroutine.
 */
public typealias MessageEndpointListenerChangeSuspendListener = ChangeSuspendListener<MessageEndpointListenerChange>
