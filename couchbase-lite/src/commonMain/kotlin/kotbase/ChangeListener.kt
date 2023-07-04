package kotbase

public typealias ChangeListener<T> = (changed: T) -> Unit

public typealias SuspendChangeListener<T> = suspend (changed: T) -> Unit
