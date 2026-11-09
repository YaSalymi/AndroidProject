package com.example.androidproject

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

// 1. Статусы и чистая модель данных
enum class ShuttleStatus(val label: String) {
    DOCKED("В доке"),
    IN_FLIGHT("В полёте"),
    CRITICAL_FAILURE("Авария")
}

data class ShuttleItem(
    val id: String,
    val name: String,
    val fuel: Int = 100,
    val hull: Int = 100,
    val cargo: Int = 0,
    val status: ShuttleStatus = ShuttleStatus.DOCKED
) {
    val isCritical: Boolean = status == ShuttleStatus.CRITICAL_FAILURE
    val canLoadCargo: Boolean = status == ShuttleStatus.DOCKED && cargo < 50
    val canLaunch: Boolean = status == ShuttleStatus.DOCKED && fuel >= 30 && hull >= 50
    val canJump: Boolean = status == ShuttleStatus.IN_FLIGHT && fuel >= 40
    val canDock: Boolean = status == ShuttleStatus.IN_FLIGHT
}

data class FleetUiState(
    val shuttles: List<ShuttleItem> = emptyList()
)

// 2. ViewModel (хранилище состояния и бизнес-логика)
class FleetViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FleetUiState())
    val uiState: StateFlow<FleetUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = FleetUiState(
            shuttles = listOf(
                ShuttleItem("1", "Шаттл «Восток-1»", 100, 100, 0, ShuttleStatus.DOCKED),
                ShuttleItem("2", "Шаттл «Буран-02»", 45, 80, 20, ShuttleStatus.DOCKED),
                ShuttleItem("3", "Шаттл «Союз-X»", 60, 70, 30, ShuttleStatus.IN_FLIGHT),
                ShuttleItem("4", "Шаттл «Орион-9»", 15, 90, 10, ShuttleStatus.DOCKED),
                ShuttleItem("5", "Шаттл «Протон-М»", 80, 30, 40, ShuttleStatus.DOCKED),
                ShuttleItem("6", "Шаттл «Аполлон-7»", 20, 0, 10, ShuttleStatus.CRITICAL_FAILURE),
                ShuttleItem("7", "Шаттл «Атлас-4»", 90, 95, 0, ShuttleStatus.DOCKED),
                ShuttleItem("8", "Шаттл «Гагарин-1»", 50, 65, 50, ShuttleStatus.IN_FLIGHT),
                ShuttleItem("9", "Шаттл «Зенит-12»", 85, 90, 10, ShuttleStatus.DOCKED),
                ShuttleItem("10", "Шаттл «Титан-3»", 0, 0, 30, ShuttleStatus.CRITICAL_FAILURE),
                ShuttleItem("11", "Шаттл «Вега-5»", 100, 100, 0, ShuttleStatus.DOCKED)
            )
        )
    }

    fun addShuttle() {
        val count = _uiState.value.shuttles.size + 1
        val newShuttle = ShuttleItem(
            id = UUID.randomUUID().toString(),
            name = "Шаттл «Ш-$count»"
        )
        _uiState.update { it.copy(shuttles = it.shuttles + newShuttle) }
    }

    fun removeShuttle(id: String) {
        _uiState.update { current ->
            current.copy(shuttles = current.shuttles.filterNot { it.id == id })
        }
    }

    fun loadCargo(id: String) = updateShuttle(id) {
        it.copy(cargo = (it.cargo + 10).coerceAtMost(50))
    }

    fun launchShuttle(id: String) = updateShuttle(id) {
        it.copy(status = ShuttleStatus.IN_FLIGHT)
    }

    fun makeHyperjump(id: String) = updateShuttle(id) { current ->
        val newFuel = (current.fuel - 40).coerceAtLeast(0)
        val newHull = (current.hull - 35).coerceAtLeast(0)
        val newStatus = if (newHull == 0) ShuttleStatus.CRITICAL_FAILURE else current.status
        current.copy(fuel = newFuel, hull = newHull, status = newStatus)
    }

    fun dockShuttle(id: String) = updateShuttle(id) {
        it.copy(status = ShuttleStatus.DOCKED)
    }

    fun resetShuttle(id: String) = updateShuttle(id) {
        it.copy(fuel = 100, hull = 100, cargo = 0, status = ShuttleStatus.DOCKED)
    }

    private fun updateShuttle(id: String, transform: (ShuttleItem) -> ShuttleItem) {
        _uiState.update { current ->
            current.copy(shuttles = current.shuttles.map { if (it.id == id) transform(it) else it })
        }
    }
}