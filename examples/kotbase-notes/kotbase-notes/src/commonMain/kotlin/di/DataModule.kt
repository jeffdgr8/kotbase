package di

import data.db.DatabaseProvider
import data.db.UserScopeProvider
import data.source.note.NoteRepository
import data.source.user.UserRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    single { DatabaseProvider() }
    singleOf(::UserScopeProvider)
    singleOf(::UserRepository)
    singleOf(::NoteRepository)
}
