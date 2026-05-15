package com.example.builddaily.ui.buylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.builddaily.data.model.BuyItem
import com.example.builddaily.data.model.ItemStatus
import com.example.builddaily.data.repository.BudgetData
import com.example.builddaily.data.repository.BuyListRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BuyListViewModel(private val repository: BuyListRepository) : ViewModel() {

    val items = repository.items
    val budget = repository.budget

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    // Derived states using StateFlow for efficiency
    val activeItems = items.map { list ->
        list.filter { !it.isPurchased }.sortedByDescending { it.priority.level }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchasedItems = items.map { list ->
        list.filter { it.isPurchased }.sortedByDescending { it.purchasedDate }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalWishlistValue = activeItems.map { it.sumOf { item -> item.price } }
    val totalSaved = activeItems.map { it.sumOf { item -> item.amountSaved } }
    val purchasedValue = purchasedItems.map { it.sumOf { item -> item.price } }

    val filteredItems = combine(activeItems, _selectedTab, budget) { items, tab, budget ->
        when (tab) {
            0 -> items
            1 -> items.filter { it.price <= budget.currentSavings }
            2 -> items.filter { it.itemStatus == ItemStatus.SAVING_FOR }
            else -> items
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    fun addItem(item: BuyItem) {
        viewModelScope.launch {
            repository.addItem(item)
        }
    }

    fun updateItem(item: BuyItem) {
        viewModelScope.launch {
            // Logic validation: amountSaved cannot exceed price
            val validatedItem = item.copy(
                amountSaved = item.amountSaved.coerceAtMost(item.price)
            )
            repository.updateItem(validatedItem)
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            repository.deleteItem(id)
        }
    }

    fun markAsPurchased(id: String) {
        viewModelScope.launch {
            repository.markAsPurchased(id)
        }
    }

    fun updateBudget(newBudget: BudgetData) {
        viewModelScope.launch {
            repository.updateBudget(newBudget)
        }
    }
}
