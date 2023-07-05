package kotbase

public typealias ChangeListener<T> = (changed: T) -> Unit

public typealias ChangeSuspendListener<T> = suspend (changed: T) -> Unit
