package org.neteinstein.pickaname.presentation.namelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import org.neteinstein.pickaname.domain.model.AutoRefreshResult
import org.neteinstein.pickaname.domain.model.Gender
import org.neteinstein.pickaname.domain.model.NameEntry
import org.neteinstein.pickaname.domain.model.NameFilter
import org.neteinstein.pickaname.domain.usecase.ObserveNameCountUseCase
import org.neteinstein.pickaname.domain.usecase.ObserveNamesUseCase
import org.neteinstein.pickaname.domain.usecase.RefreshNamesIfDueUseCase

data class NameListUiState(
    val names: List<NameEntry> = emptyList(),
    val count: Int = 0,
    val query: String = "",
    val selectedGender: Gender? = null,
    val selectedInitial: Char? = null,
    val traditionalOnly: Boolean = false
)

/** One-off events the name list screen should react to, e.g. by showing a Snackbar. */
sealed interface NameListEvent {
    /** The periodic auto-refresh check ran and failed; the existing data is untouched. */
    data object AutoRefreshFailed : NameListEvent
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class NameListViewModel(
    private val observeNamesUseCase: ObserveNamesUseCase,
    private val observeNameCountUseCase: ObserveNameCountUseCase,
    private val refreshNamesIfDueUseCase: RefreshNamesIfDueUseCase
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val gender = MutableStateFlow<Gender?>(null)
    private val initial = MutableStateFlow<Char?>(null)
    private val traditionalOnly = MutableStateFlow(false)

    private val _events = Channel<NameListEvent>(Channel.BUFFERED)
    val events: Flow<NameListEvent> = _events.receiveAsFlow()

    /**
     * [query]'s very first value passes through immediately; only later edits are debounced.
     * Without this split, `combine` below would wait out the full debounce window before
     * producing *any* result, delaying gender/initial selections made right as the screen opens.
     */
    private val debouncedQuery: Flow<String> = merge(query.take(1), query.drop(1).debounce(250))

    /**
     * Deliberately a plain [Flow], not a [StateFlow]. An intermediate `stateIn(Eagerly, ...)`
     * here would run as its own independent collector, racing against [uiState]'s subscription:
     * `uiState` could see a stale/default filter snapshot before this producer's first real
     * value lands, emitting one incorrect transient result. Keeping it as a plain flow means
     * [uiState]'s own subscription is what drives this combine, so there is exactly one
     * producer and no stale-snapshot race.
     */
    private val filter: Flow<NameFilter> =
        combine(debouncedQuery, gender, initial, traditionalOnly) { q, g, i, traditional ->
            NameFilter(query = q, gender = g, initial = i, traditionalOnly = traditional)
        }

    val uiState: StateFlow<NameListUiState> = filter.flatMapLatest { currentFilter ->
        combine(
            observeNamesUseCase(currentFilter),
            observeNameCountUseCase(currentFilter),
            // Raw, non-debounced query so the search field always echoes what was just typed.
            // Using `currentFilter.query` here instead would tie the *displayed* text to the same
            // 250ms debounce/DB round-trip that throttles `names`/`count`, so the field would
            // never catch up while the user is actively typing (it looked like input was ignored).
            query
        ) { names, count, rawQuery ->
            NameListUiState(
                names = names,
                count = count,
                query = rawQuery,
                selectedGender = currentFilter.gender,
                selectedInitial = currentFilter.initial,
                traditionalOnly = currentFilter.traditionalOnly
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NameListUiState())

    init {
        // Reaching this screen means the database is already populated (splash routes to sync
        // otherwise), so this is the right place for the once-per-app-open periodic refresh
        // check. A success silently refreshes the list below via the reactive Flow above; a
        // failure only surfaces a transient message - existing data is never touched.
        viewModelScope.launch {
            if (refreshNamesIfDueUseCase() is AutoRefreshResult.Failed) {
                _events.send(NameListEvent.AutoRefreshFailed)
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    fun onGenderSelected(selected: Gender?) {
        gender.value = selected
    }

    fun onInitialSelected(selected: Char?) {
        initial.value = selected
    }

    fun onTraditionalOnlyChanged(enabled: Boolean) {
        traditionalOnly.value = enabled
    }
}
