package kotbase

/**
 * **ENTERPRISE EDITION API**
 *
 * The MessagingCompletion callback interface used for acknowledging
 * the completion of a messaging operation.
 *
 * Acknowledges completion of the close operation.
 */
public typealias MessagingCloseCompletion = () -> Unit
