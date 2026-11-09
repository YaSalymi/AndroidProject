package com.example.androidproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.NavEntry

// Ключи маршрутов для Navigation 3
sealed interface ScreenKey {
    data object FleetList : ScreenKey
    data class ShuttleDetail(val shuttleId: String) : ScreenKey
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: FleetViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    // В Navigation 3 стек навигации хранится реактивным списком
    val backStack = rememberSaveable {
        mutableStateListOf<ScreenKey>(ScreenKey.FleetList)
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { key: ScreenKey ->
            when (key) {
                is ScreenKey.FleetList -> NavEntry(key) {
                    FleetListScreen(
                        state = uiState,
                        onShuttleClick = { shuttleId ->
                            backStack.add(ScreenKey.ShuttleDetail(shuttleId))
                        },
                        onAddShuttle = { viewModel.addShuttle() },
                        onDeleteShuttle = { id -> viewModel.removeShuttle(id) }
                    )
                }

                is ScreenKey.ShuttleDetail -> NavEntry(key) {
                    val shuttle = uiState.shuttles.find { it.id == key.shuttleId }
                    if (shuttle != null) {
                        ShuttleDetailScreen(
                            shuttle = shuttle,
                            onBack = { backStack.removeLastOrNull() },
                            onLoadCargo = { viewModel.loadCargo(shuttle.id) },
                            onLaunch = { viewModel.launchShuttle(shuttle.id) },
                            onJump = { viewModel.makeHyperjump(shuttle.id) },
                            onDock = { viewModel.dockShuttle(shuttle.id) },
                            onReset = { viewModel.resetShuttle(shuttle.id) }
                        )
                    } else {
                        backStack.removeLastOrNull()
                        Text("Шаттл не найден")
                    }
                }
            }
        }
    )
}