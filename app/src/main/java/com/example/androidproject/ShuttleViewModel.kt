package com.example.androidproject

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

// --- ЭКРАН 1: СПИСОК ФЛОТА ---

data class FleetListUiState(
    val shuttles: List<ShuttleItem> = emptyList()
)

class FleetListViewModel(
    private val repo: FleetRepository
) : ViewModel() {

    val uiState: FleetListUiState
        get() = FleetListUiState(shuttles = repo.shuttles)

    fun onAddShuttle() = repo.addShuttle()
    fun onRemoveShuttle(id: String) = repo.removeShuttle(id)
}

@Composable
fun FleetListRoute(
    repo: FleetRepository,
    onOpenShuttle: (String) -> Unit,
    onOpenSummary: () -> Unit
) {
    val viewModel = viewModel { FleetListViewModel(repo) }
    FleetListScreen(
        state = viewModel.uiState,
        onShuttleClick = onOpenShuttle,
        onSummaryClick = onOpenSummary,
        onAddShuttle = viewModel::onAddShuttle,
        onDeleteShuttle = viewModel::onRemoveShuttle
    )
}

// --- ЭКРАН 2: КАРТОЧКА ШАТТЛА ---

data class ShuttleDetailUiState(
    val shuttle: ShuttleItem? = null
)

class ShuttleDetailViewModel(
    private val shuttleId: String,
    private val repo: FleetRepository
) : ViewModel() {

    val uiState: ShuttleDetailUiState
        get() = ShuttleDetailUiState(shuttle = repo.findShuttle(shuttleId))

    fun onLoadCargo() = repo.loadCargo(shuttleId)
    fun onLaunch() = repo.launchShuttle(shuttleId)
    fun onJump() = repo.makeHyperjump(shuttleId)
    fun onDock() = repo.dockShuttle(shuttleId)
    fun onReset() = repo.resetShuttle(shuttleId)
}

@Composable
fun ShuttleDetailRoute(
    shuttleId: String,
    repo: FleetRepository,
    onBack: () -> Unit
) {
    val viewModel = viewModel { ShuttleDetailViewModel(shuttleId, repo) }
    ShuttleDetailScreen(
        state = viewModel.uiState,
        onBack = onBack,
        onLoadCargo = viewModel::onLoadCargo,
        onLaunch = viewModel::onLaunch,
        onJump = viewModel::onJump,
        onDock = viewModel::onDock,
        onReset = viewModel::onReset
    )
}

// --- ЭКРАН 3: ЦЕНТР УПРАВЛЕНИЯ ФЛОТОМ (СВОДКА) ---

data class FleetSummaryUiState(
    val totalCount: Int = 0,
    val dockedCount: Int = 0,
    val inFlightCount: Int = 0,
    val criticalCount: Int = 0,
    val totalCargo: Int = 0,
    val canLaunchAny: Boolean = false,
    val canDockAny: Boolean = false
)

class FleetSummaryViewModel(
    private val repo: FleetRepository
) : ViewModel() {

    val uiState: FleetSummaryUiState
        get() {
            val list = repo.shuttles
            return FleetSummaryUiState(
                totalCount = list.size,
                dockedCount = list.count { it.status == ShuttleStatus.DOCKED },
                inFlightCount = list.count { it.status == ShuttleStatus.IN_FLIGHT },
                criticalCount = list.count { it.status == ShuttleStatus.CRITICAL_FAILURE },
                totalCargo = list.sumOf { it.cargo },
                canLaunchAny = list.any { it.canLaunch },
                canDockAny = list.any { it.canDock }
            )
        }

    fun onLaunchAll() = repo.launchAllAvailable()
    fun onDockAll() = repo.dockAllInFlight()
}

@Composable
fun FleetSummaryRoute(
    repo: FleetRepository,
    onBack: () -> Unit
) {
    val viewModel = viewModel { FleetSummaryViewModel(repo) }
    FleetSummaryScreen(
        state = viewModel.uiState,
        onBack = onBack,
        onLaunchAll = viewModel::onLaunchAll,
        onDockAll = viewModel::onDockAll
    )
}