package org.neteinstein.pickaname.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.neteinstein.pickaname.domain.usecase.ObserveNeedsInitialSyncUseCase

/** Where the splash screen should navigate to once it's done deciding. */
enum class SplashDestination {
    SYNC,
    NAME_LIST
}

class SplashViewModel(
    private val observeNeedsInitialSyncUseCase: ObserveNeedsInitialSyncUseCase
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            // Kick off the (fast, local) DB check in parallel with the minimum splash duration,
            // so the brand moment is never skipped even when the check resolves instantly.
            val needsSyncDeferred = async { observeNeedsInitialSyncUseCase().first() }
            delay(MIN_SPLASH_DURATION_MS)
            val needsSync = needsSyncDeferred.await()
            _destination.value = if (needsSync) SplashDestination.SYNC else SplashDestination.NAME_LIST
        }
    }

    private companion object {
        const val MIN_SPLASH_DURATION_MS = 900L
    }
}
