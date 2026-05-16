package com.example.builddaily.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.builddaily.data.model.BuyItem
import com.example.builddaily.data.model.BuyPriority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class BuyListRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("buy_list_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _items = MutableStateFlow<List<BuyItem>>(loadItems())
    val items: StateFlow<List<BuyItem>> = _items.asStateFlow()

    private val _budget = MutableStateFlow(loadBudget())
    val budget: StateFlow<BudgetData> = _budget.asStateFlow()

    private fun loadItems(): List<BuyItem> {
        val jsonStr = prefs.getString("buy_items", "[]") ?: "[]"
        return try { json.decodeFromString<List<BuyItem>>(jsonStr) } catch (e: Exception) { emptyList() }
    }

    private fun saveItems(items: List<BuyItem>) {
        prefs.edit().putString("buy_items", json.encodeToString(items)).apply()
        _items.value = items
    }

    private fun loadBudget(): BudgetData {
        val currentMonth = kotlinx.datetime.Clock.System.now().toString().substring(0, 7) // YYYY-MM
        val lastResetMonth = prefs.getString("last_reset_month", "") ?: ""
        
        var budget = BudgetData(
            monthlyBudget = getSafeDouble("monthly_budget", 1000.0),
            savingsGoal = getSafeDouble("savings_goal", 500.0),
            spentThisMonth = getSafeDouble("spent_this_month", 0.0),
            lastResetMonth = lastResetMonth
        )

        // Monthly Reset System
        if (lastResetMonth != currentMonth) {
            budget = budget.copy(spentThisMonth = 0.0, lastResetMonth = currentMonth)
            persistBudget(budget)
        }
        
        return budget
    }

    private fun getSafeDouble(key: String, default: Double): Double {
        return try {
            Double.fromBits(prefs.getLong(key, default.toRawBits()))
        } catch (e: Exception) {
            try {
                val floatVal = prefs.getFloat(key, default.toFloat())
                prefs.edit().remove(key).putLong(key, floatVal.toDouble().toRawBits()).apply()
                floatVal.toDouble()
            } catch (e2: Exception) {
                default
            }
        }
    }

    private fun persistBudget(budget: BudgetData) {
        prefs.edit()
            .putLong("monthly_budget", budget.monthlyBudget.toRawBits())
            .putLong("savings_goal", budget.savingsGoal.toRawBits())
            .putLong("spent_this_month", budget.spentThisMonth.toRawBits())
            .putString("last_reset_month", budget.lastResetMonth)
            .apply()
    }

    private fun saveBudget(budget: BudgetData) {
        persistBudget(budget)
        _budget.value = budget
    }

    fun updateBudget(budget: BudgetData) {
        saveBudget(budget)
    }

    fun addItem(item: BuyItem) {
        val updated = _items.value + item
        saveItems(updated)
    }

    fun updateItem(item: BuyItem) {
        val updated = _items.value.map { if (it.id == item.id) item else it }
        saveItems(updated)
    }

    fun deleteItem(id: String) {
        val updated = _items.value.filter { it.id != id }
        saveItems(updated)
    }

    fun markAsPurchased(id: String) {
        var purchasedPrice = 0.0
        val updated = _items.value.map {
            if (it.id == id) {
                purchasedPrice = it.price
                it.copy(
                    isPurchased = true,
                    purchasedDate = kotlinx.datetime.Clock.System.now().toString().substringBefore("T"),
                    finalPurchasePrice = it.price
                )
            } else it
        }
        saveItems(updated)
        
        val currentBudget = _budget.value
        updateBudget(currentBudget.copy(spentThisMonth = currentBudget.spentThisMonth + purchasedPrice))
    }

    fun undoPurchase(id: String) {
        var purchasedPrice = 0.0
        val updated = _items.value.map {
            if (it.id == id && it.isPurchased) {
                purchasedPrice = it.finalPurchasePrice ?: it.price
                it.copy(
                    isPurchased = false,
                    purchasedDate = null,
                    finalPurchasePrice = null
                )
            } else it
        }
        saveItems(updated)
        
        val currentBudget = _budget.value
        updateBudget(currentBudget.copy(spentThisMonth = (currentBudget.spentThisMonth - purchasedPrice).coerceAtLeast(0.0)))
    }

    fun duplicateItem(id: String) {
        val item = _items.value.find { it.id == id } ?: return
        val newItem = item.copy(
            id = java.util.UUID.randomUUID().toString(),
            name = "${item.name} (Copy)",
            isPurchased = false,
            purchasedDate = null,
            isArchived = false,
            isDeleted = false,
            deletedDate = null,
            createdAt = kotlinx.datetime.Clock.System.now().toString()
        )
        addItem(newItem)
    }

    fun softDeleteItem(id: String) {
        val updated = _items.value.map {
            if (it.id == id) it.copy(
                isDeleted = true,
                deletedDate = kotlinx.datetime.Clock.System.now().toString().substringBefore("T")
            ) else it
        }
        saveItems(updated)
    }

    fun restoreItem(id: String) {
        val updated = _items.value.map {
            if (it.id == id) it.copy(isDeleted = false, deletedDate = null) else it
        }
        saveItems(updated)
    }

    fun permanentDeleteItem(id: String) {
        val updated = _items.value.filter { it.id != id }
        saveItems(updated)
    }

    fun archiveItem(id: String) {
        val updated = _items.value.map {
            if (it.id == id) it.copy(isArchived = true) else it
        }
        saveItems(updated)
    }

    fun unarchiveItem(id: String) {
        val updated = _items.value.map {
            if (it.id == id) it.copy(isArchived = false) else it
        }
        saveItems(updated)
    }

    fun getActiveItems(): List<BuyItem> = _items.value
        .filter { !it.isPurchased && !it.isDeleted && !it.isArchived }
        .sortedByDescending { it.priority.level }

    fun getPurchasedItems(): List<BuyItem> = _items.value
        .filter { it.isPurchased && !it.isDeleted }
        .sortedByDescending { it.purchasedDate }

    fun getArchivedItems(): List<BuyItem> = _items.value
        .filter { it.isArchived && !it.isDeleted }
        .sortedByDescending { it.createdAt }

    fun getDeletedItems(): List<BuyItem> = _items.value
        .filter { it.isDeleted }
        .sortedByDescending { it.deletedDate }

    fun getTotalWishlistValue(): Double = getActiveItems().sumOf { it.price }

    fun getAffordableItems(): List<BuyItem> = getActiveItems().filter { it.price <= _budget.value.remainingAvailable }
}

data class BudgetData(
    val monthlyBudget: Double = 1000.0,
    val savingsGoal: Double = 500.0,
    val spentThisMonth: Double = 0.0,
    val lastResetMonth: String = "" // Format: YYYY-MM
) {
    val wishlistSpendingBudget: Double
        get() = (monthlyBudget - savingsGoal).coerceAtLeast(0.0)

    val remainingAvailable: Double
        get() = (wishlistSpendingBudget - spentThisMonth).coerceAtLeast(0.0)
}