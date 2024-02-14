package di

import domain.replication.ReplicationService
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object Di : KoinComponent {

    val replicationService: ReplicationService get() = get()
}
