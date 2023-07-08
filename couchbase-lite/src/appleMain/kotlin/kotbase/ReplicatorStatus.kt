package kotbase

import cocoapods.CouchbaseLite.CBLReplicatorStatus
import kotbase.base.DelegatedClass
import kotbase.ext.toCouchbaseLiteException

public actual class ReplicatorStatus
internal constructor(actual: CBLReplicatorStatus) :
    DelegatedClass<CBLReplicatorStatus>(actual) {

    public actual val activityLevel: ReplicatorActivityLevel by lazy {
        ReplicatorActivityLevel.from(actual.activity)
    }

    public actual val progress: ReplicatorProgress by lazy {
        ReplicatorProgress(actual.progress)
    }

    public actual val error: CouchbaseLiteException? by lazy {
        actual.error?.toCouchbaseLiteException()
    }

    override fun toString(): String = "Status{activityLevel=$activityLevel, progress=$progress, error=$error}"
}
