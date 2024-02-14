package di

import domain.replication.AuthService
import domain.replication.ReplicationService
import domain.replication.SyncGateway
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val domainModule = module {
    single {
        SyncGateway(
            useTls = false,
            url = "localhost",
            port = 4984,
            databaseName = "kotbase-notes"
        )
    }
    singleOf(::AuthService)
    singleOf(::ReplicationService)
}
