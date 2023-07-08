package kotbase

public actual class ReplicatorChange
internal constructor(
    public actual val replicator: Replicator,
    public actual val status: ReplicatorStatus
) {

    override fun toString(): String = "ReplicatorChange{$replicator => $status}"
}
