package org.neteinstein.pickaname.presentation.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.neteinstein.pickaname.domain.model.SyncFailureReason
import org.neteinstein.pickaname.domain.model.SyncOutcome
import org.neteinstein.pickaname.domain.usecase.GetSourceUrlUseCase
import org.neteinstein.pickaname.domain.usecase.SyncNamesUseCase

/** Where this sync run was triggered from — used only to pick a distinct nav-graph route. */
enum class SyncOrigin {
    ONBOARDING,
    SETTINGS
}

sealed interface SyncUiState {
    data object Loading : SyncUiState
    data class Error(val reason: SyncFailureReason) : SyncUiState
    data object Success : SyncUiState
}

/**
 * Downloads + parses + persists the names list on entry. Used both for the very first run
 * (empty DB) and whenever the user changes the source URL from Settings.
 */
class SyncViewModel(
    private val getSourceUrlUseCase: GetSourceUrlUseCase,
    private val syncNamesUseCase: SyncNamesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SyncUiState>(SyncUiState.Loading)
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    init {
        runSync()
    }

    fun retry() {
        _uiState.value = SyncUiState.Loading
        runSync()
    }

    private fun runSync() {
        viewModelScope.launch {
            val url = getSourceUrlUseCase()
            _uiState.value = when (val outcome = syncNamesUseCase(url)) {
                is SyncOutcome.Success -> SyncUiState.Success
                is SyncOutcome.Error -> SyncUiState.Error(outcome.reason)
            }
        }
    }
}
