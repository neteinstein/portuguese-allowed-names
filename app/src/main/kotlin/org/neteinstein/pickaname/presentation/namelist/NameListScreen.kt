package org.neteinstein.pickaname.presentation.namelist

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.neteinstein.pickaname.R
import org.neteinstein.pickaname.domain.model.Gender
import org.neteinstein.pickaname.domain.model.NameEntry
import org.neteinstein.pickaname.presentation.common.GenderTag
import org.neteinstein.pickaname.presentation.theme.PickANameExtendedColors
import org.neteinstein.pickaname.presentation.theme.PickANameTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * Main screen: the full names list with gender/initial filters and a live match count. Reachable
 * only once the database has been populated (splash routes elsewhere otherwise). On entry, the
 * view model runs the periodic auto-refresh check silently; if it fails, a Snackbar reports it
 * without disturbing the (still valid) data already on screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameListScreen(
    onOpenSettings: () -> Unit,
    viewModel: NameListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val autoRefreshFailedMessage = stringResource(R.string.name_list_auto_refresh_failed)
    val listState = rememberLazyListState()
    var showRulesSheet by remember { mutableStateOf(false) }
    var showTraditionalNamesInfoSheet by remember { mutableStateOf(false) }
    var randomlyPickedName by remember { mutableStateOf<NameEntry?>(null) }
    var nameMeaningSearch by remember { mutableStateOf<NameEntry?>(null) }
    val context = LocalContext.current

    // The fast scroller only makes sense over the full alphabetical list: with a single
    // initial selected there's only ever one letter on screen, so jumping between letters
    // has nothing to do.
    val letterIndex = remember(uiState.names) { buildLetterIndex(uiState.names) }
    val showLetterScroller = uiState.selectedInitial == null && letterIndex.size > 1

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is NameListEvent.AutoRefreshFailed -> snackbarHostState.showSnackbar(autoRefreshFailedMessage)
            }
        }
    }

    if (showRulesSheet) {
        ModalBottomSheet(onDismissRequest = { showRulesSheet = false }) {
            RulesBottomSheetContent()
        }
    }

    if (showTraditionalNamesInfoSheet) {
        ModalBottomSheet(onDismissRequest = { showTraditionalNamesInfoSheet = false }) {
            TraditionalNamesInfoBottomSheetContent()
        }
    }

    randomlyPickedName?.let { entry ->
        RandomNameDialog(entry = entry, onDismiss = { randomlyPickedName = null })
    }

    nameMeaningSearch?.let { entry ->
        ModalBottomSheet(onDismissRequest = { nameMeaningSearch = null }) {
            NameMeaningBottomSheetContent(entry = entry)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { randomlyPickedName = uiState.names.random() },
                        enabled = uiState.names.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Casino,
                            contentDescription = stringResource(R.string.cd_random_name_icon)
                        )
                    }
                    IconButton(onClick = { showRulesSheet = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = stringResource(R.string.cd_name_rules_icon)
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.cd_settings_icon)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchField(
                query = uiState.query,
                onQueryChange = viewModel::onQueryChange
            )

            Text(
                text = stringResource(R.string.filter_section_gender),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            GenderFilterRow(
                selected = uiState.selectedGender,
                onSelected = viewModel::onGenderSelected
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.filter_section_other),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            OtherFiltersRow(
                traditionalOnly = uiState.traditionalOnly,
                onTraditionalOnlyChanged = viewModel::onTraditionalOnlyChanged,
                onTraditionalNamesInfoClick = { showTraditionalNamesInfoSheet = true }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.filter_section_initial),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            InitialFilterRow(
                selected = uiState.selectedInitial,
                onSelected = viewModel::onInitialSelected
            )

            AnimatedContent(
                targetState = uiState.count,
                transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
                label = "nameCount"
            ) { count ->
                Text(
                    text = pluralStringResource(R.plurals.name_count, count, count),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            AnimatedContent(
                targetState = uiState.names.isEmpty(),
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                label = "nameListContent",
                modifier = Modifier.fillMaxSize()
            ) { isEmpty ->
                if (isEmpty) {
                    EmptyState()
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                top = 8.dp,
                                end = if (showLetterScroller) 32.dp else 16.dp,
                                bottom = 8.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.names, key = { it.id }) { entry ->
                                NameRow(
                                    entry = entry,
                                    modifier = Modifier.animateItem(),
                                    onLongPress = {
                                        if (canLoadWebView(context)) {
                                            nameMeaningSearch = entry
                                        } else {
                                            openUrl(context, buildMeaningSearchUrl(context, entry))
                                        }
                                    }
                                )
                            }
                        }

                        if (showLetterScroller) {
                            LetterFastScroller(
                                letterIndex = letterIndex,
                                listState = listState,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .fillMaxHeight()
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Official Government of Portugal pages linked from the rules bottom sheet below. Kept as plain
 * constants (not string resources) since URLs aren't translatable content.
 */
private const val NAME_RULES_URL =
    "https://irn.justica.gov.pt/Servicos/Cidadao/Nascimento/Composicao-do-nome"
private const val ALLOWED_NAMES_PDF_URL =
    "https://irn.justica.gov.pt/Portals/33/Regras%20Nome%20Pr%C3%B3prio/Lista%20Nomes%20Pr%C3%B3prios.pdf?ver=WNDmmwiSO3uacofjmNoxEQ%3D%3D"
private const val REGISTER_BIRTH_URL = "https://justica.gov.pt/Servicos/Registar-nascimento"

private data class OfficialResourceLink(
    val title: String,
    val description: String,
    val url: String
)

@Composable
private fun RulesBottomSheetContent() {
    val context = LocalContext.current
    val officialLinks = listOf(
        OfficialResourceLink(
            title = stringResource(R.string.name_rules_link_rules_title),
            description = stringResource(R.string.name_rules_link_rules_description),
            url = NAME_RULES_URL
        ),
        OfficialResourceLink(
            title = stringResource(R.string.name_rules_link_pdf_title),
            description = stringResource(R.string.name_rules_link_pdf_description),
            url = ALLOWED_NAMES_PDF_URL
        ),
        OfficialResourceLink(
            title = stringResource(R.string.name_rules_link_register_birth_title),
            description = stringResource(R.string.name_rules_link_register_birth_description),
            url = REGISTER_BIRTH_URL
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.name_rules_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.name_rules_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        RulesDisclaimerCard(text = stringResource(R.string.name_rules_disclaimer))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            officialLinks.forEach { link ->
                OfficialResourceLinkRow(
                    link = link,
                    onClick = { openUrl(context, link.url) }
                )
            }
        }
        Text(
            text = stringResource(R.string.name_rules_footer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RulesDisclaimerCard(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.WarningAmber,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun OfficialResourceLinkRow(link: OfficialResourceLink, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = link.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = link.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun TraditionalNamesInfoBottomSheetContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.traditional_names_info_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.traditional_names_info_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        RulesDisclaimerCard(text = stringResource(R.string.traditional_names_info_disclaimer))
        Text(
            text = stringResource(R.string.traditional_names_info_rules),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/** Opens [url] in an external browser; silently no-ops if no app can handle it. */
private fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    } catch (e: ActivityNotFoundException) {
        // No browser available on this device - nothing else we can do here.
    }
}

/** A Google search for what [entry]'s name means, phrased in the app's current language. */
private fun buildMeaningSearchUrl(context: Context, entry: NameEntry): String {
    val queryText = context.getString(R.string.name_meaning_search_query, entry.name)
    return "https://www.google.com".toUri().buildUpon()
        .appendEncodedPath("search")
        .appendQueryParameter("q", queryText)
        .build()
        .toString()
}

/**
 * Whether this device can actually host a [WebView]. Some OEM/enterprise-restricted devices ship
 * without a working WebView provider, which throws when instantiated - in that case the in-app
 * bottom sheet isn't an option and callers should fall back to [openUrl] instead.
 */
private fun canLoadWebView(context: Context): Boolean =
    try {
        WebView(context)
        true
    } catch (e: Throwable) {
        false
    }

/** Bottom sheet content: a Google search for [entry]'s meaning, rendered in an embedded WebView. */
@Composable
private fun NameMeaningBottomSheetContent(entry: NameEntry) {
    val context = LocalContext.current
    val searchUrl = remember(entry) { buildMeaningSearchUrl(context, entry) }

    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f)) {
        Text(
            text = entry.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        )
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    // Keeps taps on search results loading inside this WebView instead of
                    // spawning external intents, so exploring results stays in the sheet.
                    webViewClient = WebViewClient()
                    loadUrl(searchUrl)
                }
            }
        )
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text(stringResource(R.string.name_list_search_hint)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            AnimatedVisibility(visible = query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cd_clear_search)
                    )
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SearchOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.name_list_empty_state),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GenderFilterRow(selected: Gender?, onSelected: (Gender?) -> Unit) {
    val extendedColors = PickANameTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelected(null) },
            label = { Text(stringResource(R.string.filter_gender_all)) }
        )
        GenderFilterChip(
            label = stringResource(R.string.gender_female),
            icon = Icons.Filled.Female,
            selected = selected == Gender.FEMALE,
            onClick = { onSelected(Gender.FEMALE) },
            containerColor = extendedColors.femaleContainer,
            contentColor = extendedColors.onFemaleContainer
        )
        GenderFilterChip(
            label = stringResource(R.string.gender_male),
            icon = Icons.Filled.Male,
            selected = selected == Gender.MALE,
            onClick = { onSelected(Gender.MALE) },
            containerColor = extendedColors.maleContainer,
            contentColor = extendedColors.onMaleContainer
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenderFilterChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = containerColor,
            selectedLabelColor = contentColor,
            selectedLeadingIconColor = contentColor
        )
    )
}

@Composable
private fun OtherFiltersRow(
    traditionalOnly: Boolean,
    onTraditionalOnlyChanged: (Boolean) -> Unit,
    onTraditionalNamesInfoClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        FilterChip(
            selected = traditionalOnly,
            onClick = { onTraditionalOnlyChanged(!traditionalOnly) },
            label = { Text(stringResource(R.string.filter_traditional_names)) },
            modifier = Modifier.padding(start = 8.dp)
        )
        IconButton(onClick = onTraditionalNamesInfoClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = stringResource(R.string.cd_traditional_names_info_icon),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun InitialFilterRow(selected: Char?, onSelected: (Char?) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelected(null) },
                label = { Text(stringResource(R.string.filter_initial_all)) }
            )
        }
        items(('A'..'Z').toList()) { letter ->
            FilterChip(
                selected = selected == letter,
                onClick = { onSelected(letter) },
                label = { Text(letter.toString()) }
            )
        }
    }
}

/** First index of each initial letter in [names], in the order the letters first appear. */
private fun buildLetterIndex(names: List<NameEntry>): Map<Char, Int> {
    val firstIndexByLetter = linkedMapOf<Char, Int>()
    names.forEachIndexed { index, entry ->
        val letter = entry.name.firstOrNull()?.uppercaseChar() ?: return@forEachIndexed
        firstIndexByLetter.getOrPut(letter) { index }
    }
    return firstIndexByLetter
}

private val FAST_SCROLLER_LETTERS = ('A'..'Z').toList()

/**
 * Nearest letter to [from] that actually has entries, so touching a gap in the alphabet (e.g. no
 * names start with "K") still jumps somewhere useful instead of doing nothing.
 */
private fun nearestAvailableLetter(from: Char, letterIndex: Map<Char, Int>): Char? {
    if (letterIndex.containsKey(from)) return from
    for (offset in 1 until FAST_SCROLLER_LETTERS.size) {
        val after = from + offset
        if (after <= 'Z' && letterIndex.containsKey(after)) return after
        val before = from - offset
        if (before >= 'A' && letterIndex.containsKey(before)) return before
    }
    return null
}

/**
 * A-Z fast-scroll rail pinned to the trailing edge of the name list, styled after the Google
 * Photos date scrubber: press or drag anywhere along it to jump straight to that letter's
 * section, with a floating bubble echoing the letter under the finger while touched.
 */
@Composable
private fun LetterFastScroller(
    letterIndex: Map<Char, Int>,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    var activeLetter by remember { mutableStateOf<Char?>(null) }
    var scrollTargetIndex by remember { mutableStateOf<Int?>(null) }
    var touchYPx by remember { mutableFloatStateOf(0f) }
    var stripHeightPx by remember { mutableFloatStateOf(0f) }
    val haptics = LocalHapticFeedback.current
    val fastScrollerDescription = stringResource(R.string.cd_letter_fast_scroller)

    LaunchedEffect(scrollTargetIndex) {
        scrollTargetIndex?.let { listState.scrollToItem(it) }
    }

    fun handleTouch(y: Float) {
        touchYPx = y
        if (stripHeightPx <= 0f) return
        val fraction = (y / stripHeightPx).coerceIn(0f, 0.9999f)
        val rawLetter = FAST_SCROLLER_LETTERS[(fraction * FAST_SCROLLER_LETTERS.size).toInt()]
        val resolvedLetter = nearestAvailableLetter(rawLetter, letterIndex) ?: return
        if (resolvedLetter != activeLetter) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        activeLetter = resolvedLetter
        scrollTargetIndex = letterIndex[resolvedLetter]
    }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(20.dp)
                .semantics { contentDescription = fastScrollerDescription }
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                    RoundedCornerShape(10.dp)
                )
                .onSizeChanged { stripHeightPx = it.height.toFloat() }
                .pointerInput(letterIndex) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        handleTouch(down.position.y)
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            change.consume()
                            handleTouch(change.position.y)
                        }
                        activeLetter = null
                        scrollTargetIndex = null
                    }
                },
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FAST_SCROLLER_LETTERS.forEach { letter ->
                val isAvailable = letterIndex.containsKey(letter)
                Text(
                    text = letter.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (activeLetter == letter) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        activeLetter == letter -> MaterialTheme.colorScheme.primary
                        isAvailable -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    }
                )
            }
        }

        activeLetter?.let { letter ->
            val bubbleSizePx = with(LocalDensity.current) { 56.dp.roundToPx() }
            val gapPx = with(LocalDensity.current) { 8.dp.roundToPx() }
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = -(bubbleSizePx + gapPx),
                            y = (touchYPx - bubbleSizePx / 2f)
                                .roundToInt()
                                .coerceIn(0, (stripHeightPx - bubbleSizePx).toInt().coerceAtLeast(0))
                        )
                    }
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter.toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun NameRow(entry: NameEntry, onLongPress: () -> Unit, modifier: Modifier = Modifier) {
    val extendedColors = PickANameTheme.extendedColors
    val (avatarContainer, avatarContent) = genderAvatarColors(entry.gender, extendedColors)
    val haptics = LocalHapticFeedback.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPress()
                    }
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(avatarContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.name.take(1).uppercase(),
                    color = avatarContent,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = entry.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            GenderTag(gender = entry.gender)
        }
    }
}

@Composable
private fun genderAvatarColors(
    gender: Gender,
    extendedColors: PickANameExtendedColors
): Pair<Color, Color> = when (gender) {
    Gender.FEMALE -> extendedColors.femaleContainer to extendedColors.onFemaleContainer
    Gender.MALE -> extendedColors.maleContainer to extendedColors.onMaleContainer
}

/**
 * Full-screen overlay showing the dice's pick, celebrated with looping firework bursts behind
 * it. Backed by [Dialog] so the system back button dismisses it for free; tapping the scrim
 * (anywhere outside the card) dismisses it too, while a tap on the card itself is consumed so it
 * doesn't propagate to the scrim underneath it.
 */
@Composable
private fun RandomNameDialog(entry: NameEntry, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            FireworksOverlay(modifier = Modifier.fillMaxSize())
            RandomNameCard(entry = entry)
        }
    }
}

@Composable
private fun RandomNameCard(entry: NameEntry) {
    val extendedColors = PickANameTheme.extendedColors
    val (avatarContainer, avatarContent) = genderAvatarColors(entry.gender, extendedColors)

    Card(
        // Consumes its own taps so tapping the card doesn't fall through to the scrim behind it.
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {}
        ),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(avatarContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.name.take(1).uppercase(),
                    color = avatarContent,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            Text(
                text = entry.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            GenderTag(gender = entry.gender)
        }
    }
}

private data class FireworkParticle(val angle: Float, val color: Color)

private data class FireworkBurst(
    val center: Offset,
    val phaseOffset: Float,
    val particles: List<FireworkParticle>
)

private val FIREWORK_COLORS = listOf(
    Color(0xFFFFC107),
    Color(0xFFFF5252),
    Color(0xFF69F0AE),
    Color(0xFF40C4FF),
    Color(0xFFE040FB)
)

/** Relative (0..1 of width/height) positions for each firework burst, staggered around the card. */
private val FIREWORK_BURST_CENTERS = listOf(
    Offset(0.18f, 0.22f),
    Offset(0.82f, 0.18f),
    Offset(0.14f, 0.78f),
    Offset(0.86f, 0.8f),
    Offset(0.5f, 0.1f)
)

private const val FIREWORK_PARTICLES_PER_BURST = 16
private const val FIREWORK_LOOP_MILLIS = 1400

/**
 * Looping firework bursts radiating outward from a handful of points around the screen. Each
 * burst is offset in time ([FireworkBurst.phaseOffset]) so they don't all pop at once, then fade
 * back in once their cycle wraps - giving a continuous celebration for as long as the dialog
 * stays open.
 */
@Composable
private fun FireworksOverlay(modifier: Modifier = Modifier) {
    val bursts = remember {
        FIREWORK_BURST_CENTERS.mapIndexed { index, center ->
            FireworkBurst(
                center = center,
                phaseOffset = index / FIREWORK_BURST_CENTERS.size.toFloat(),
                particles = List(FIREWORK_PARTICLES_PER_BURST) {
                    FireworkParticle(
                        angle = Random.nextFloat() * (2 * PI).toFloat(),
                        color = FIREWORK_COLORS.random()
                    )
                }
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "fireworks")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(FIREWORK_LOOP_MILLIS, easing = LinearEasing)),
        label = "fireworksTime"
    )

    Canvas(modifier = modifier) {
        val maxRadius = size.minDimension * 0.22f
        bursts.forEach { burst ->
            val progress = (time + burst.phaseOffset) % 1f
            val alpha = if (progress < 0.15f) progress / 0.15f else (1f - progress).coerceIn(0f, 1f)
            if (alpha <= 0f) return@forEach
            val burstCenter = Offset(burst.center.x * size.width, burst.center.y * size.height)
            val distance = maxRadius * progress
            burst.particles.forEach { particle ->
                val point = burstCenter + Offset(
                    x = cos(particle.angle) * distance,
                    y = sin(particle.angle) * distance
                )
                drawCircle(
                    color = particle.color.copy(alpha = alpha),
                    radius = 3.dp.toPx() * (1f - progress * 0.5f),
                    center = point
                )
            }
        }
    }
}
