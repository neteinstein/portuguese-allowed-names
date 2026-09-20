package org.neteinstein.pickaname.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.neteinstein.pickaname.domain.model.NamesSourceDefaults
import org.neteinstein.pickaname.domain.model.RefreshPeriod
import org.neteinstein.pickaname.domain.model.SearchEngine
import org.neteinstein.pickaname.domain.usecase.GetRefreshPeriodUseCase
import org.neteinstein.pickaname.domain.usecase.GetSearchEngineUseCase
import org.neteinstein.pickaname.domain.usecase.GetSourceUrlUseCase
import org.neteinstein.pickaname.domain.usecase.ResetSourceUrlUseCase
import org.neteinstein.pickaname.domain.usecase.UpdateRefreshPeriodUseCase
import org.neteinstein.pickaname.domain.usecase.UpdateSearchEngineUseCase
import org.neteinstein.pickaname.domain.usecase.UpdateSourceUrlUseCase

data class SettingsUiState(
    val sourceUrl: String = "",
    val urlError: Boolean = false,
    val refreshPeriod: RefreshPeriod = RefreshPeriod.DEFAULT,
    val searchEngine: SearchEngine = SearchEngine.DEFAULT
)

/** One-off events the Settings screen should react to (e.g. by navigating to the sync screen). */
sealed interface SettingsEvent {
    data object SourceUpdated : SettingsEvent
}

class SettingsViewModel(
    private val getSourceUrlUseCase: GetSourceUrlUseCase,
    private val updateSourceUrlUseCase: UpdateSourceUrlUseCase,
    private val resetSourceUrlUseCase: ResetSourceUrlUseCase,
    private val getRefreshPeriodUseCase: GetRefreshPeriodUseCase,
    private val updateRefreshPeriodUseCase: UpdateRefreshPeriodUseCase,
    private val getSearchEngineUseCase: GetSearchEngineUseCase,
    private val updateSearchEngineUseCase: UpdateSearchEngineUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events: Flow<SettingsEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(sourceUrl = getSourceUrlUseCase()) }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(refreshPeriod = getRefreshPeriodUseCase()) }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(searchEngine = getSearchEngineUseCase()) }
        }
    }

    fun onUrlChange(newUrl: String) {
        _uiState.update { it.copy(sourceUrl = newUrl, urlError = false) }
    }

    fun onSave() {
        viewModelScope.launch {
            val result = updateSourceUrlUseCase(_uiState.value.sourceUrl)
            if (result.isSuccess) {
                _uiState.update { it.copy(urlError = false) }
                _events.send(SettingsEvent.SourceUpdated)
            } else {
                _uiState.update { it.copy(urlError = true) }
            }
        }
    }

    fun onReset() {
        viewModelScope.launch {
            resetSourceUrlUseCase()
            _uiState.update {
                it.copy(sourceUrl = NamesSourceDefaults.DEFAULT_SOURCE_URL, urlError = false)
            }
            _events.send(SettingsEvent.SourceUpdated)
        }
    }

    /** Persists immediately - unlike the source URL, a cadence change needs no resync/reload. */
    fun onRefreshPeriodSelected(period: RefreshPeriod) {
        _uiState.update { it.copy(refreshPeriod = period) }
        viewModelScope.launch {
            updateRefreshPeriodUseCase(period)
        }
    }

    /** Persists immediately - like refresh cadence, a search-engine change needs no resync/reload. */
    fun onSearchEngineSelected(engine: SearchEngine) {
        _uiState.update { it.copy(searchEngine = engine) }
        viewModelScope.launch {
            updateSearchEngineUseCase(engine)
        }
    }
}
