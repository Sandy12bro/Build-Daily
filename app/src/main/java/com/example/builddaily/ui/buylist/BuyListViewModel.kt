package com.example.builddaily.ui.buylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.builddaily.data.model.BuyItem
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
    val activeItems = repository.items.map { repository.getActiveItems() }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val purchasedItems = repository.items.map { repository.getPurchasedItems() }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val archivedItems = repository.items.map { repository.getArchivedItems() }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val deletedItems = repository.items.map { repository.getDeletedItems() }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalWishlistValue = activeItems.map { it.sumOf { item -> item.price } }
    val totalSaved = activeItems.map { it.sumOf { item -> item.amountSaved } }
    val purchasedValue = purchasedItems.map { it.sumOf { item -> item.price } }

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

    fun undoPurchase(id: String) {
        viewModelScope.launch {
            repository.undoPurchase(id)
        }
    }

    fun duplicateItem(id: String) {
        viewModelScope.launch {
            repository.duplicateItem(id)
        }
    }

    fun softDeleteItem(id: String) {
        viewModelScope.launch {
            repository.softDeleteItem(id)
        }
    }

    fun restoreItem(id: String) {
        viewModelScope.launch {
            repository.restoreItem(id)
        }
    }

    fun permanentDeleteItem(id: String) {
        viewModelScope.launch {
            repository.permanentDeleteItem(id)
        }
    }

    fun archiveItem(id: String) {
        viewModelScope.launch {
            repository.archiveItem(id)
        }
    }

    fun unarchiveItem(id: String) {
        viewModelScope.launch {
            repository.unarchiveItem(id)
        }
    }

    fun updateBudget(newBudget: BudgetData) {
        viewModelScope.launch {
            repository.updateBudget(newBudget)
        }
    }

    fun quickAddSavings(item: BuyItem, amount: Double) {
        val newSaved = item.amountSaved + amount
        updateItem(item.copy(amountSaved = newSaved))
        
        if (newSaved >= item.price) {
            markAsPurchased(item.id)
        }
    }
}
