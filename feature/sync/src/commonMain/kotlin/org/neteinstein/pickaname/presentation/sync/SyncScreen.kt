package org.neteinstein.pickaname.presentation.sync

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.neteinstein.pickaname.domain.model.SyncFailureReason
import org.neteinstein.pickaname.domain.platform.PlatformCapabilities
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.neteinstein.pickaname.core.designsystem.resources.Res
import org.neteinstein.pickaname.core.designsystem.resources.sync_edit_source
import org.neteinstein.pickaname.core.designsystem.resources.sync_error_invalid_source
import org.neteinstein.pickaname.core.designsystem.resources.sync_error_network
import org.neteinstein.pickaname.core.designsystem.resources.sync_error_no_names_found
import org.neteinstein.pickaname.core.designsystem.resources.sync_error_title
import org.neteinstein.pickaname.core.designsystem.resources.sync_error_unknown
import org.neteinstein.pickaname.core.designsystem.resources.sync_loading_message
import org.neteinstein.pickaname.core.designsystem.resources.sync_loading_title
import org.neteinstein.pickaname.core.designsystem.resources.sync_retry

/**
 * Shown on first run and whenever the names source URL changes. Downloads, parses and persists
 * the names list, showing a loading message while it works and a retry/edit-source recovery UI
 * on failure (the default source is a real government server observed to be flaky).
 */
@Composable
fun SyncScreen(
    onSyncSuccess: () -> Unit,
    onEditSource: () -> Unit,
    viewModel: SyncViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is SyncUiState.Success) {
            onSyncSuccess()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = uiState,
            contentKey = { it is SyncUiState.Error },
            transitionSpec = {
                fadeIn(tween(320)) togetherWith fadeOut(tween(200))
            },
            label = "syncState"
        ) { state ->
            when (state) {
                is SyncUiState.Loading, is SyncUiState.Success -> LoadingContent()
                is SyncUiState.Error -> ErrorContent(
                    reason = state.reason,
                    onRetry = viewModel::retry,
                    onEditSource = onEditSource
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val infiniteTransition = rememberInfiniteTransition(label = "syncRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(1600, easing = LinearEasing)),
        label = "syncIconRotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(400)) + scaleIn(
                initialScale = 0.7f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Sync,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .size(44.dp)
                        .rotate(rotation)
                )
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = stringResource(Res.string.sync_loading_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.sync_loading_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(28.dp))
        LinearProgressIndicator(
            modifier = Modifier
                .width(160.dp)
                .clip(RoundedCornerShape(50)),
        )
    }
}

@Composable
private fun ErrorContent(
    reason: SyncFailureReason,
    onRetry: () -> Unit,
    onEditSource: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(400)) + scaleIn(
                initialScale = 0.7f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(Res.string.sync_error_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(reason.messageRes()),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(28.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // No source to edit on web - it reads the published snapshot (see
            // PlatformCapabilities.canConfigureNamesSource), so retry is the only useful action.
            if (PlatformCapabilities.canConfigureNamesSource) {
                OutlinedButton(
                    onClick = onEditSource,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                    Text(stringResource(Res.string.sync_edit_source))
                }
            }
            Button(
                onClick = onRetry,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(stringResource(Res.string.sync_retry))
            }
        }
    }
}

private fun SyncFailureReason.messageRes(): StringResource = when (this) {
    SyncFailureReason.NETWORK -> Res.string.sync_error_network
    SyncFailureReason.INVALID_SOURCE -> Res.string.sync_error_invalid_source
    SyncFailureReason.NO_NAMES_FOUND -> Res.string.sync_error_no_names_found
    SyncFailureReason.UNKNOWN -> Res.string.sync_error_unknown
}
