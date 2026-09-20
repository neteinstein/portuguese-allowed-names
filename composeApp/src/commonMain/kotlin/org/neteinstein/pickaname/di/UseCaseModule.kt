package org.neteinstein.pickaname.di

import org.koin.dsl.module
import org.neteinstein.pickaname.domain.usecase.GetLastRefreshTimestampUseCase
import org.neteinstein.pickaname.domain.usecase.GetRefreshPeriodUseCase
import org.neteinstein.pickaname.domain.usecase.GetSearchEngineUseCase
import org.neteinstein.pickaname.domain.usecase.GetSourceUrlUseCase
import org.neteinstein.pickaname.domain.usecase.ObserveNameCountUseCase
import org.neteinstein.pickaname.domain.usecase.ObserveNamesUseCase
import org.neteinstein.pickaname.domain.usecase.ObserveNeedsInitialSyncUseCase
import org.neteinstein.pickaname.domain.usecase.ObserveSearchEngineUseCase
import org.neteinstein.pickaname.domain.usecase.ObserveSourceUrlUseCase
import org.neteinstein.pickaname.domain.usecase.RefreshNamesIfDueUseCase
import org.neteinstein.pickaname.domain.usecase.ResetSourceUrlUseCase
import org.neteinstein.pickaname.domain.usecase.SyncNamesUseCase
import org.neteinstein.pickaname.domain.usecase.UpdateRefreshPeriodUseCase
import org.neteinstein.pickaname.domain.usecase.UpdateSearchEngineUseCase
import org.neteinstein.pickaname.domain.usecase.UpdateSourceUrlUseCase

val useCaseModule = module {
    factory { ObserveNamesUseCase(get()) }
    factory { ObserveNameCountUseCase(get()) }
    factory { ObserveNeedsInitialSyncUseCase(get()) }
    factory { ObserveSourceUrlUseCase(get()) }
    factory { GetSourceUrlUseCase(get()) }
    factory { UpdateSourceUrlUseCase(get()) }
    factory { ResetSourceUrlUseCase(get()) }
    factory { GetRefreshPeriodUseCase(get()) }
    factory { GetLastRefreshTimestampUseCase(get()) }
    factory { UpdateRefreshPeriodUseCase(get()) }
    factory { GetSearchEngineUseCase(get()) }
    factory { UpdateSearchEngineUseCase(get()) }
    factory { ObserveSearchEngineUseCase(get()) }
    factory { SyncNamesUseCase(get(), get()) }
    factory { RefreshNamesIfDueUseCase(get(), get()) }
}
