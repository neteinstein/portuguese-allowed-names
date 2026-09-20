package org.neteinstein.pickaname.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.neteinstein.pickaname.presentation.namelist.NameListViewModel
import org.neteinstein.pickaname.presentation.settings.SettingsViewModel
import org.neteinstein.pickaname.presentation.splash.SplashViewModel
import org.neteinstein.pickaname.presentation.sync.SyncViewModel

val viewModelModule = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::SyncViewModel)
    viewModelOf(::NameListViewModel)
    viewModelOf(::SettingsViewModel)
}
