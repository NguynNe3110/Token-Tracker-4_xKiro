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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAccounts: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Token Tracker") },
                actions = {
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
            item {
                Text(
                    text = "Today's Usage",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            items(state.accountUsages, key = { it.account.id }) { usage ->
                AccountUsageCard(usage = usage)
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
private fun AccountUsageCard(usage: AccountUsage) {
    val percent = if (usage.limitPerDay > 0) {
        (usage.usedToday.toFloat() / usage.limitPerDay).coerceIn(0f, 1f)
    } else 0f
    val progressColor = getProgressColor(percent)
    val percentText = if (usage.limitPerDay > 0) {
        "%.1f%%".format(usage.usedToday.toFloat() / usage.limitPerDay * 100)
    } else "N/A"

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = usage.account.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${formatNumber(usage.usedToday.toInt())} / ${formatNumber(usage.limitPerDay.toInt())} ($percentText)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = percent.coerceIn(0f, 1f))
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(progressColor)
                )
            }
        }
    }
}

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

private fun formatNumber(value: Int): String = when {
    value >= 1_000_000 -> "%.1fM".format(value / 1_000_000.0)
    value >= 1_000 -> "%.1fK".format(value / 1_000.0)
    else -> value.toString()
}
