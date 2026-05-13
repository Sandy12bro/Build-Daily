package com.example.builddaily.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.builddaily.data.model.BuyCategory
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
        return BudgetData(
            monthlyBudget = prefs.getFloat("monthly_budget", 500f).toDouble(),
            savingsGoal = prefs.getFloat("savings_goal", 200f).toDouble(),
            currentSavings = prefs.getFloat("current_savings", 0f).toDouble(),
            essentialReserve = prefs.getFloat("essential_reserve", 1000f).toDouble()
        )
    }

    private fun saveBudget(budget: BudgetData) {
        prefs.edit()
            .putFloat("monthly_budget", budget.monthlyBudget.toFloat())
            .putFloat("savings_goal", budget.savingsGoal.toFloat())
            .putFloat("current_savings", budget.currentSavings.toFloat())
            .putFloat("essential_reserve", budget.essentialReserve.toFloat())
            .apply()
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
        val updated = _items.value.map {
            if (it.id == id) it.copy(
                isPurchased = true,
                purchasedDate = kotlinx.datetime.Clock.System.now().toString().substringBefore("T")
            ) else it
        }
        saveItems(updated)
    }

    fun getActiveItems(): List<BuyItem> = _items.value
        .filter { !it.isPurchased }
        .sortedByDescending { it.priority.level }

    fun getPurchasedItems(): List<BuyItem> = _items.value
        .filter { it.isPurchased }
        .sortedByDescending { it.purchasedDate }

    fun getTotalWishlistValue(): Double = getActiveItems().sumOf { it.price }

    fun getAffordableItems(): List<BuyItem> = getActiveItems().filter { it.price <= _budget.value.currentSavings }
}

data class BudgetData(
    val monthlyBudget: Double = 500.0,
    val savingsGoal: Double = 200.0,
    val currentSavings: Double = 0.0,
    val essentialReserve: Double = 1000.0
) {
    val monthlySavingsRate: Double
        get() = currentSavings
}