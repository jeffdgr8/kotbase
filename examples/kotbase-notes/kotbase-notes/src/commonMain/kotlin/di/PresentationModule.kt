package di

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import presentation.AppViewModel
import presentation.EditViewModel
import presentation.LoginViewModel
import presentation.MainViewModel

val presentationModule = module {
    factoryOf(::AppViewModel)
    factoryOf(::LoginViewModel)
    factoryOf(::MainViewModel)
    factoryOf(::EditViewModel)
}
