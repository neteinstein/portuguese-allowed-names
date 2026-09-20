package org.neteinstein.pickaname.presentation.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.neteinstein.pickaname.core.designsystem.R
import org.neteinstein.pickaname.domain.model.RefreshPeriod
import org.neteinstein.pickaname.domain.model.SearchEngine

/**
 * Settings screen: link out to the OS per-app language picker, a dropdown for which search
 * engine long-pressing a name should use, a form to view/edit/reset the PDF source URL, and a
 * dropdown to configure how often the app should automatically re-check that source. Saving or
 * resetting the URL fires [SettingsEvent.SourceUpdated], which the caller uses to navigate to the
 * sync screen (re-downloading and re-parsing with the new source); the search engine and refresh
 * cadence both apply immediately on selection since neither needs a resync.
 *
 * The search engine section is hidden by default and only revealed for the current screen
 * visit after the top bar title is tapped 10 times, since most users never need to change it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSourceUpdated: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.SourceUpdated -> onSourceUpdated()
            }
        }
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    var titleTapCount by remember { mutableStateOf(0) }
    var searchEngineSectionVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            titleTapCount++
                            if (titleTapCount >= 10) {
                                searchEngineSectionVisible = true
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(350)) + slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight / 8 },
                animationSpec = tween(350)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsSectionCard(
                    icon = Icons.Filled.Language,
                    title = stringResource(R.string.settings_language_section),
                    description = stringResource(R.string.settings_language_description)
                ) {
                    OutlinedButton(
                        onClick = { openAppLocaleSettings(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.settings_language_button))
                    }
                }

                if (searchEngineSectionVisible) {
                    SettingsSectionCard(
                        icon = Icons.Filled.Search,
                        title = stringResource(R.string.settings_search_engine_section),
                        description = stringResource(R.string.settings_search_engine_description)
                    ) {
                        EnumDropdown(
                            selected = uiState.searchEngine,
                            options = SearchEngine.entries,
                            label = stringResource(R.string.settings_search_engine_label),
                            optionLabel = { stringResource(it.labelRes()) },
                            onSelected = viewModel::onSearchEngineSelected
                        )
                    }
                }

                SettingsSectionCard(
                    icon = Icons.Filled.Link,
                    title = stringResource(R.string.settings_source_section),
                    description = stringResource(R.string.settings_source_description)
                ) {
                    OutlinedTextField(
                        value = uiState.sourceUrl,
                        onValueChange = viewModel::onUrlChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.settings_source_url_label)) },
                        isError = uiState.urlError,
                        supportingText = {
                            if (uiState.urlError) {
                                Text(stringResource(R.string.settings_source_url_error))
                            }
                        },
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = viewModel::onReset,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.settings_reset))
                        }
                        Button(
                            onClick = viewModel::onSave,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.settings_save))
                        }
                    }
                }

                SettingsSectionCard(
                    icon = Icons.Filled.Schedule,
                    title = stringResource(R.string.settings_refresh_section),
                    description = stringResource(R.string.settings_refresh_description)
                ) {
                    EnumDropdown(
                        selected = uiState.refreshPeriod,
                        options = RefreshPeriod.entries,
                        label = stringResource(R.string.settings_refresh_period_label),
                        optionLabel = { stringResource(it.labelRes()) },
                        onSelected = viewModel::onRefreshPeriodSelected
                    )
                }
            }
        }
    }
}

/** A rounded, tonal card grouping one settings topic behind a leading icon badge + title. */
@Composable
private fun SettingsSectionCard(
    icon: ImageVector,
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
            content()
        }
    }
}

/** A read-only dropdown for picking one of [options], the enum's label rendered via [optionLabel]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EnumDropdown(
    selected: T,
    options: List<T>,
    label: String,
    optionLabel: @Composable (T) -> String,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            readOnly = true,
            value = optionLabel(selected),
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun RefreshPeriod.labelRes(): Int = when (this) {
    RefreshPeriod.WEEKLY -> R.string.refresh_period_weekly
    RefreshPeriod.MONTHLY -> R.string.refresh_period_monthly
    RefreshPeriod.QUARTERLY -> R.string.refresh_period_quarterly
    RefreshPeriod.BI_YEARLY -> R.string.refresh_period_bi_yearly
    RefreshPeriod.YEARLY -> R.string.refresh_period_yearly
}

private fun SearchEngine.labelRes(): Int = when (this) {
    SearchEngine.GOOGLE -> R.string.search_engine_google
    SearchEngine.DUCKDUCKGO -> R.string.search_engine_duckduckgo
    SearchEngine.BRAVE -> R.string.search_engine_brave
}

private fun openAppLocaleSettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    }.apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}
