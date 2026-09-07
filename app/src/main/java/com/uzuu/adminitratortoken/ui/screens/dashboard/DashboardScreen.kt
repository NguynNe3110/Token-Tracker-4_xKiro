package com.uzuu.adminitratortoken.ui.screens.dashboard

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.DecimalFormat


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAccounts: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.importFromFile(context, it)
        }
    }

    LaunchedEffect(viewModel.importResultMessage) {
        viewModel.importResultMessage.collect { msg ->
            if (msg != null) {
                snackbarHostState.showSnackbar(msg)
                viewModel.clearImportResult()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Token Tracker") },
                actions = {
                    IconButton(onClick = { importLauncher.launch("text/plain") }) {
                        Icon(Icons.Filled.FileOpen, contentDescription = "Import accounts")
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                        }
                    }
                    IconButton(onClick = onNavigateToAccounts) {
                        Icon(Icons.Filled.AccountCircle, contentDescription = "Accounts")
                    }
                }
            )
        }
    ) { paddingValues ->
        DashboardContent(
            state = uiState,
            onToggleAutoRefresh = viewModel::toggleAutoRefresh,
            paddingValues = paddingValues
        )
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState,
    onToggleAutoRefresh: () -> Unit,
    paddingValues: androidx.compose.foundation.layout.PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AutoRefreshRow(
                enabled = state.autoRefreshEnabled,
                secondsUntilNext = state.secondsUntilNextRefresh,
                onToggle = onToggleAutoRefresh
            )
        }

        if (state.accountUsages.isEmpty()) {
            item { EmptyAccountsHint() }
        } else {
            itemsIndexed(
                items = state.accountUsages,
                key = { _, usage -> usage.account.id }
            ) { index, usage ->
                AccountUsageRow(usage = usage)
                if (index < state.accountUsages.lastIndex) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AutoRefreshRow(
    enabled: Boolean,
    secondsUntilNext: Int,
    onToggle: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Auto Refresh",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            if (enabled && secondsUntilNext > 0) {
                val minutes = secondsUntilNext / 60
                val seconds = secondsUntilNext % 60
                Text(
                    text = "Next: ${minutes}:${seconds.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Switch(checked = enabled, onCheckedChange = { onToggle() })
        }
    }
}

@Composable
private fun EmptyAccountsHint() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No accounts yet",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap the user icon to add an account",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun AccountUsageRow(usage: AccountUsage) {
    // Calculate usage percentage for progress bar
    val percent = if (usage.limitPerDay > 0) {
        usage.usedToday.toFloat() / usage.limitPerDay
    } else 0f
    val progressColor = getProgressColor(percent)

    // Format usage as "used / limitM" with dot thousand separators (e.g., "3.232.123 / 5M")
    val usageText = if (usage.limitPerDay > 0) {
        val limitInM = usage.limitPerDay / 1_000_000
        "${formatNumber(usage.usedToday)}/${formatNumber(limitInM)}M"
    } else {
        "N/A"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        // Top row: account name and usage text
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = usage.account.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = usageText,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
            )
        }
//        Spacer(modifier = Modifier.height(4.dp))
        // Bottom row: progress bar
        LinearProgressIndicator(
            progress = { percent.coerceIn(0f, 1f) },
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}

private val numberFormat = DecimalFormat("#,###").apply {
    val symbols = decimalFormatSymbols
    symbols.groupingSeparator = '.'
    decimalFormatSymbols = symbols
}

private fun formatNumber(value: Long): String = numberFormat.format(value)

private fun getProgressColor(percent: Float): Color {
    val p = (percent * 100).coerceIn(0f, 100f)
    return when {
        p <= 8f   -> Color(0xFF00B4D8)
        p <= 16f  -> Color(0xFF48CAE4)
        p <= 25f  -> Color(0xFF90E0EF)
        p <= 33f  -> Color(0xFFB5E48C)
        p <= 41f  -> Color(0xFF7CB518)
        p <= 50f  -> Color(0xFFF3D34A)
        p <= 58f  -> Color(0xFFF7B731)
        p <= 66f  -> Color(0xFFF5A623)
        p <= 75f  -> Color(0xFFF28C28)
        p <= 83f  -> Color(0xFFE67E22)
        p <= 91f  -> Color(0xFFD63031)
        else      -> Color(0xFFC0392B)
    }
}
