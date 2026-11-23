package com.example.androidproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ShuttleStatus(val label: String) {
    DOCKED("В доке базы"),
    IN_FLIGHT("В полёте"),
    CRITICAL_FAILURE("Авария систем")
}

// Единый объект состояния (State)
data class ShuttleUiState(
    val fuel: Int = 100,
    val hull: Int = 100,
    val cargo: Int = 0,
    val status: ShuttleStatus = ShuttleStatus.DOCKED
) {
    val isCritical: Boolean = status == ShuttleStatus.CRITICAL_FAILURE
    val canLoadCargo: Boolean = status == ShuttleStatus.DOCKED && cargo < 50
    // Комплексное правило от нескольких свойств:
    val canLaunch: Boolean = status == ShuttleStatus.DOCKED && fuel >= 30 && hull >= 50
    // Расход 25% позволяет сделать 3 прыжка (100 -> 75 -> 50 -> 25) и довести корпус (100 -> 65 -> 30 -> 0) до аварии
    val canJump: Boolean = status == ShuttleStatus.IN_FLIGHT && fuel >= 25
    val canDock: Boolean = status == ShuttleStatus.IN_FLIGHT
}

// Saver позволяет сохранять весь объект ShuttleUiState в rememberSaveable без лишних плагинов
val ShuttleStateSaver = listSaver<ShuttleUiState, Any>(
    save = { state -> listOf(state.fuel, state.hull, state.cargo, state.status.name) },
    restore = { saved ->
        ShuttleUiState(
            fuel = saved[0] as Int,
            hull = saved[1] as Int,
            cargo = saved[2] as Int,
            status = ShuttleStatus.valueOf(saved[3] as String)
        )
    }
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ShuttleApp()
                }
            }
        }
    }
}

// Корневой App composable (Stateful — владеет состоянием)
@Composable
fun ShuttleApp(modifier: Modifier = Modifier) {
    var uiState by rememberSaveable(stateSaver = ShuttleStateSaver) {
        mutableStateOf(ShuttleUiState())
    }

    ShuttleScreen(
        state = uiState,
        onLoadCargo = {
            uiState = uiState.copy(cargo = (uiState.cargo + 10).coerceAtMost(50))
        },
        onLaunch = {
            uiState = uiState.copy(status = ShuttleStatus.IN_FLIGHT)
        },
        onJump = {
            val newFuel = (uiState.fuel - 25).coerceAtLeast(0)
            val newHull = (uiState.hull - 35).coerceAtLeast(0)
            val newStatus = if (newHull == 0) ShuttleStatus.CRITICAL_FAILURE else uiState.status
            uiState = uiState.copy(
                fuel = newFuel,
                hull = newHull,
                status = newStatus
            )
        },
        onDock = {
            uiState = uiState.copy(status = ShuttleStatus.DOCKED)
        },
        onReset = {
            uiState = ShuttleUiState()
        },
        modifier = modifier
    )
}

// Самостоятельный переиспользуемый экран (Stateless)
@Composable
fun ShuttleScreen(
    state: ShuttleUiState,
    onLoadCargo: () -> Unit,
    onLaunch: () -> Unit,
    onJump: () -> Unit,
    onDock: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (state.isCritical) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.background

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Бортовой терминал «Шаттл-1»",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        if (state.isCritical) {
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Статус: ${state.status.label}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (state.isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Топливо:")
                        Text("${state.fuel} / 100%")
                    }
                    LinearProgressIndicator(
                        progress = { state.fuel / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Целостность корпуса:")
                        Text("${state.hull} / 100%")
                    }
                    LinearProgressIndicator(
                        progress = { state.hull / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (state.hull < 30) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Загрузка трюма:")
                    Text("${state.cargo} / 50 т")
                }
            }
        }

        Text(
            text = "Панель управления",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Button(
            onClick = onLoadCargo,
            enabled = state.canLoadCargo,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Загрузить контейнер (+10 т)")
        }

        Button(
            onClick = onLaunch,
            enabled = state.canLaunch,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Начать экспедицию")
        }
        if (state.status == ShuttleStatus.DOCKED && !state.canLaunch) {
            Text(
                text = "Для вылета требуется минимум 30% топлива и 50% прочности корпуса.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Button(
            onClick = onJump,
            enabled = state.canJump,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Совершить гиперпрыжок (-25% топл., -35% корп.)")
        }

        Button(
            onClick = onDock,
            enabled = state.canDock,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Пристыковаться к доку")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
            colors = if (state.isCritical) {
                ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            } else {
                ButtonDefaults.outlinedButtonColors()
            }
        ) {
            Text(if (state.isCritical) "Аварийная эвакуация и ремонт (Сброс)" else "Техническое обслуживание (Сброс)")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShuttleScreenPreview() {
    MaterialTheme {
        ShuttleScreen(
            state = ShuttleUiState(fuel = 75, hull = 65, cargo = 20, status = ShuttleStatus.IN_FLIGHT),
            onLoadCargo = {},
            onLaunch = {},
            onJump = {},
            onDock = {},
            onReset = {}
        )
    }
}