package kotbase

public actual class DocumentReplication
internal constructor(
    public actual val replicator: Replicator,
    public actual val isPush: Boolean,
    public actual val documents: List<ReplicatedDocument>
) {

    override fun toString(): String = "DocumentReplication{#${documents.size} @$replicator}"
}
