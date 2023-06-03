package kotbase

public typealias ConflictHandler = (document: MutableDocument, oldDocument: Document?) -> Boolean
