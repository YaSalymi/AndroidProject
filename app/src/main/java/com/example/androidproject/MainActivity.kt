package com.example.androidproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

@Serializable
data object FleetListKey : NavKey

@Serializable
data class ShuttleDetailKey(val shuttleId: String) : NavKey

@Serializable
data object FleetSummaryKey : NavKey

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = (application as ShuttleApp).repository
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FleetNavigation(repo = repo)
                }
            }
        }
    }
}

@Composable
fun FleetNavigation(repo: FleetRepository) {
    val backStack = rememberNavBackStack(FleetListKey)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<FleetListKey> {
                FleetListRoute(
                    repo = repo,
                    onOpenShuttle = { shuttleId ->
                        backStack.add(ShuttleDetailKey(shuttleId))
                    },
                    onOpenSummary = {
                        backStack.add(FleetSummaryKey)
                    }
                )
            }

            entry<ShuttleDetailKey> { key ->
                ShuttleDetailRoute(
                    shuttleId = key.shuttleId,
                    repo = repo,
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<FleetSummaryKey> {
                FleetSummaryRoute(
                    repo = repo,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}