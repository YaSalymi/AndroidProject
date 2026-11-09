package com.example.androidproject

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- ЭКРАН 1: СПИСОК ОБЪЕКТОВ ---
@Composable
fun FleetListScreen(
    state: FleetUiState,
    onShuttleClick: (String) -> Unit,
    onAddShuttle: () -> Unit,
    onDeleteShuttle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Космический флот",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Button(onClick = onAddShuttle) {
                Text("+ Новый")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(state.shuttles, key = { it.id }) { shuttle ->
                val cardBg = if (shuttle.isCritical) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onShuttleClick(shuttle.id) },
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = shuttle.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = shuttle.status.label,
                                fontWeight = FontWeight.SemiBold,
                                color = if (shuttle.isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Топливо: ${shuttle.fuel}% | Корпус: ${shuttle.hull}% | Трюм: ${shuttle.cargo} т",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { onDeleteShuttle(shuttle.id) }) {
                                Text("Списать", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- ЭКРАН 2: ДЕТАЛИ ШАТТЛА ---
@Composable
fun ShuttleDetailScreen(
    shuttle: ShuttleItem,
    onBack: () -> Unit,
    onLoadCargo: () -> Unit,
    onLaunch: () -> Unit,
    onJump: () -> Unit,
    onDock: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (shuttle.isCritical) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.background

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Назад к флоту")
        }

        Text(
            text = shuttle.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        if (shuttle.isCritical) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ТРЕВОГА: Корпус разрушен! Автоматика заблокировала основные функции шаттла.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Статус: ${shuttle.status.label}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (shuttle.isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Топливо:")
                        Text("${shuttle.fuel} / 100%")
                    }
                    LinearProgressIndicator(
                        progress = { shuttle.fuel / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Целостность корпуса:")
                        Text("${shuttle.hull} / 100%")
                    }
                    LinearProgressIndicator(
                        progress = { shuttle.hull / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (shuttle.hull < 30) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Загрузка трюма:")
                    Text("${shuttle.cargo} / 50 т")
                }
            }
        }

        Text("Панель управления", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Button(
            onClick = onLoadCargo,
            enabled = shuttle.canLoadCargo,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Загрузить контейнер (+10 т)")
        }

        Button(
            onClick = onLaunch,
            enabled = shuttle.canLaunch,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Начать экспедицию")
        }
        if (shuttle.status == ShuttleStatus.DOCKED && !shuttle.canLaunch) {
            Text(
                text = "Для вылета требуется минимум 30% топлива и 50% прочности корпуса.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Button(
            onClick = onJump,
            enabled = shuttle.canJump,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Совершить гиперпрыжок (-40% топл., -35% корп.)")
        }

        Button(
            onClick = onDock,
            enabled = shuttle.canDock,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Пристыковаться к доку")
        }

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
            colors = if (shuttle.isCritical) {
                ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            } else {
                ButtonDefaults.outlinedButtonColors()
            }
        ) {
            Text(if (shuttle.isCritical) "Аварийная эвакуация и ремонт (Сброс)" else "Техническое обслуживание (Сброс)")
        }
    }
}