package com.example.androidproject

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.UUID

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
    val canJump: Boolean = status == ShuttleStatus.IN_FLIGHT && fuel >= 25
    val canDock: Boolean = status == ShuttleStatus.IN_FLIGHT
}

class FleetRepository {
    var shuttles by mutableStateOf(initialShuttles())
        private set

    fun findShuttle(id: String): ShuttleItem? = shuttles.find { it.id == id }

    fun addShuttle() {
        val count = shuttles.size + 1
        val newShuttle = ShuttleItem(
            id = UUID.randomUUID().toString(),
            name = "Шаттл «Ш-$count»"
        )
        shuttles = shuttles + newShuttle
    }

    fun removeShuttle(id: String) {
        shuttles = shuttles.filterNot { it.id == id }
    }

    fun loadCargo(id: String) = updateShuttle(id) { current ->
        if (!current.canLoadCargo) return@updateShuttle current
        current.copy(cargo = (current.cargo + 10).coerceAtMost(50))
    }

    fun launchShuttle(id: String) = updateShuttle(id) { current ->
        if (!current.canLaunch) return@updateShuttle current
        current.copy(status = ShuttleStatus.IN_FLIGHT)
    }

    fun makeHyperjump(id: String) = updateShuttle(id) { current ->
        if (!current.canJump) return@updateShuttle current
        val newFuel = (current.fuel - 25).coerceAtLeast(0)
        val newHull = (current.hull - 35).coerceAtLeast(0)
        val newStatus = if (newHull == 0) ShuttleStatus.CRITICAL_FAILURE else current.status
        current.copy(fuel = newFuel, hull = newHull, status = newStatus)
    }

    fun dockShuttle(id: String) = updateShuttle(id) { current ->
        if (!current.canDock) return@updateShuttle current
        current.copy(status = ShuttleStatus.DOCKED)
    }

    fun resetShuttle(id: String) = updateShuttle(id) {
        it.copy(fuel = 100, hull = 100, cargo = 0, status = ShuttleStatus.DOCKED)
    }

    fun launchAllAvailable() {
        shuttles = shuttles.map { shuttle ->
            if (shuttle.canLaunch) shuttle.copy(status = ShuttleStatus.IN_FLIGHT) else shuttle
        }
    }

    fun dockAllInFlight() {
        shuttles = shuttles.map { shuttle ->
            if (shuttle.canDock) shuttle.copy(status = ShuttleStatus.DOCKED) else shuttle
        }
    }

    private fun updateShuttle(id: String, transform: (ShuttleItem) -> ShuttleItem) {
        shuttles = shuttles.map { if (it.id == id) transform(it) else it }
    }

    private fun initialShuttles(): List<ShuttleItem> = listOf(
        ShuttleItem("1", "Шаттл «Восток-1»", 100, 100, 0, ShuttleStatus.DOCKED),
        ShuttleItem("2", "Шаттл «Буран-02»", 45, 80, 20, ShuttleStatus.DOCKED),
        ShuttleItem("3", "Шаттл «Союз-X»", 60, 70, 30, ShuttleStatus.IN_FLIGHT),
        ShuttleItem("4", "Шаттл «Орион-9»", 15, 90, 10, ShuttleStatus.DOCKED),
        ShuttleItem("5", "Шаттл «Протон-М»", 80, 30, 40, ShuttleStatus.DOCKED),
        ShuttleItem("6", "Шаттл «Аполлон-7»", 20, 0, 10, ShuttleStatus.CRITICAL_FAILURE),
        ShuttleItem("7", "Шаттл «Атлас-4»", 90, 95, 0, ShuttleStatus.DOCKED),
        ShuttleItem("8", "Шаттл «Гагарин-1»", 50, 65, 50, ShuttleStatus.IN_FLIGHT)
    )
}

class ShuttleApp : Application() {
    val repository = FleetRepository()
}