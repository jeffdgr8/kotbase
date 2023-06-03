package kotbase

/**
 * Custom conflict resolution strategies implement this interface.
 *
 * Callback: called when there are conflicting changes in the local
 * and remote versions of a document during replication.
 *
 * @param conflict Description of the conflicting documents.
 * @return the resolved doc.
 */
public typealias ConflictResolver = (conflict: Conflict) -> Document?
