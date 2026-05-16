package com.example.builddaily.ui.buylist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.builddaily.data.model.BuyItem
import com.example.builddaily.data.model.BuyPriority
import com.example.builddaily.data.model.ItemStatus
import com.example.builddaily.data.repository.BudgetData
import com.example.builddaily.data.repository.BuyListRepository
import com.example.builddaily.ui.theme.CyberPurple
import com.example.builddaily.ui.theme.DeepVoid
import com.example.builddaily.ui.theme.ElectricBlue
import com.example.builddaily.ui.theme.MintGreen
import com.example.builddaily.ui.theme.SolarYellow
import com.example.builddaily.util.CurrencyUtils
import com.example.builddaily.util.AnimatedCurrency
import com.example.builddaily.util.AnimatedNumber
import com.example.builddaily.util.AnimatedPercentage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyListScreen(
    onBack: () -> Unit,
    viewModel: BuyListViewModel
) {
    val items by viewModel.items.collectAsState()
    val budget by viewModel.budget.collectAsState()
    
    val activeItems by viewModel.activeItems.collectAsState()
    val purchasedItems by viewModel.purchasedItems.collectAsState()
    val archivedItems by viewModel.archivedItems.collectAsState()
    val deletedItems by viewModel.deletedItems.collectAsState()
    
    val totalWishlistValue by viewModel.totalWishlistValue.collectAsState(0.0)
    val totalSaved by viewModel.totalSaved.collectAsState(0.0)
    val purchasedValue by viewModel.purchasedValue.collectAsState(0.0)

    var showAddDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<BuyItem?>(null) }

    val mustBuyItems = activeItems.filter { it.priority == BuyPriority.MUST_BUY }
    val importantItems = activeItems.filter { it.priority == BuyPriority.IMPORTANT }
    val maybeLaterItems = activeItems.filter { it.priority == BuyPriority.MAYBE_LATER }

    Scaffold(
        containerColor = DeepVoid,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Column {
                        Text("Wishlist Planner", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Plan your dreams in steps ✨", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showBudgetDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Budget", tint = ElectricBlue)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // STEP 2 -> Monthly Safe Spending
            item {
                MonthlySafeSpendingCard(
                    budget = budget,
                    activeItemsCount = activeItems.size
                )
            }

            // STEP 1 -> Quick Add Wish Button
            item {
                QuickAddWishButton(onClick = { showAddDialog = true })
            }

            // Priority Sections & Wishlist Cards
            if (mustBuyItems.isNotEmpty()) {
                item { PrioritySectionHeader(priority = BuyPriority.MUST_BUY) }
                items(mustBuyItems, key = { it.id }) { item ->
                    SimplifiedWishlistCard(
                        item = item,
                        budget = budget,
                        onClick = { selectedItem = item },
                        onQuickSave = { amount -> viewModel.quickAddSavings(item, amount) },
                        onEdit = { selectedItem = item },
                        onDelete = { viewModel.softDeleteItem(item.id) },
                        onDuplicate = { viewModel.duplicateItem(item.id) },
                        onArchive = { viewModel.archiveItem(item.id) },
                        onMarkAsPurchased = { viewModel.markAsPurchased(item.id) }
                    )
                }
            }

            if (importantItems.isNotEmpty()) {
                item { PrioritySectionHeader(priority = BuyPriority.IMPORTANT) }
                items(importantItems, key = { it.id }) { item ->
                    SimplifiedWishlistCard(
                        item = item,
                        budget = budget,
                        onClick = { selectedItem = item },
                        onQuickSave = { amount -> viewModel.quickAddSavings(item, amount) },
                        onEdit = { selectedItem = item },
                        onDelete = { viewModel.softDeleteItem(item.id) },
                        onDuplicate = { viewModel.duplicateItem(item.id) },
                        onArchive = { viewModel.archiveItem(item.id) },
                        onMarkAsPurchased = { viewModel.markAsPurchased(item.id) }
                    )
                }
            }

            if (maybeLaterItems.isNotEmpty()) {
                item { PrioritySectionHeader(priority = BuyPriority.MAYBE_LATER) }
                items(maybeLaterItems, key = { it.id }) { item ->
                    SimplifiedWishlistCard(
                        item = item,
                        budget = budget,
                        onClick = { selectedItem = item },
                        onQuickSave = { amount -> viewModel.quickAddSavings(item, amount) },
                        onEdit = { selectedItem = item },
                        onDelete = { viewModel.softDeleteItem(item.id) },
                        onDuplicate = { viewModel.duplicateItem(item.id) },
                        onArchive = { viewModel.archiveItem(item.id) },
                        onMarkAsPurchased = { viewModel.markAsPurchased(item.id) }
                    )
                }
            }

            if (activeItems.isEmpty()) {
                item {
                    SimplifiedEmptyState(
                        onAddClick = { showAddDialog = true }
                    )
                }
            }

            // Savings Progress Summary
            if (activeItems.isNotEmpty()) {
                item {
                    SavingsProgressSummary(
                        totalSaved = totalSaved,
                        totalGoal = totalWishlistValue
                    )
                }
            }

            // This Month Purchases
            if (purchasedItems.isNotEmpty()) {
                val currentMonth = kotlinx.datetime.Clock.System.now().toString().substring(0, 7)
                val thisMonthPurchases = purchasedItems.filter { it.purchasedDate?.startsWith(currentMonth) == true }
                
                if (thisMonthPurchases.isNotEmpty()) {
                    item { ThisMonthPurchasesHeader(count = thisMonthPurchases.size) }
                    items(thisMonthPurchases, key = { "purchased_${it.id}" }) { item ->
                        AchievedGoalCard(
                            item = item,
                            onUndo = { viewModel.undoPurchase(item.id) }
                        )
                    }
                }
            }

            // Archived Items
            if (archivedItems.isNotEmpty()) {
                item { ArchivedHeader(count = archivedItems.size) }
                items(archivedItems, key = { "archived_${it.id}" }) { item ->
                    ArchivedItemCard(
                        item = item,
                        onUnarchive = { viewModel.unarchiveItem(item.id) },
                        onDelete = { viewModel.softDeleteItem(item.id) }
                    )
                }
            }

            // Recently Deleted
            if (deletedItems.isNotEmpty()) {
                item { RecentlyDeletedHeader(count = deletedItems.size) }
                items(deletedItems, key = { "deleted_${it.id}" }) { item ->
                    DeletedItemCard(
                        item = item,
                        onRestore = { viewModel.restoreItem(item.id) },
                        onPermanentDelete = { viewModel.permanentDeleteItem(item.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }

    if (showAddDialog) {
        SimpleAddWishDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { item ->
                viewModel.addItem(item)
                showAddDialog = false
            }
        )
    }

    if (showBudgetDialog) {
        SimplifiedBudgetSetupDialog(
            budget = budget,
            onDismiss = { showBudgetDialog = false },
            onSave = { newBudget ->
                viewModel.updateBudget(newBudget)
                showBudgetDialog = false
            }
        )
    }

    selectedItem?.let { item ->
        SimplifiedItemDetailSheet(
            item = item,
            budget = budget,
            onDismiss = { selectedItem = null },
            onPurchase = { viewModel.markAsPurchased(item.id); selectedItem = null },
            onDelete = { viewModel.softDeleteItem(item.id); selectedItem = null },
            onUpdateItem = { updated -> viewModel.updateItem(updated); selectedItem = updated }
        )
    }
}

@Composable
fun MonthlySafeSpendingCard(
    budget: BudgetData,
    activeItemsCount: Int
) {
    val healthStatus = when {
        budget.remainingAvailable > (budget.monthlyBudget * 0.2) -> "Safe"
        budget.remainingAvailable > (budget.monthlyBudget * 0.05) -> "Balanced"
        else -> "Tight"
    }
    val healthColor = when(healthStatus) {
        "Safe" -> MintGreen
        "Balanced" -> SolarYellow
        else -> Color(0xFFFF3B30)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            ElectricBlue.copy(alpha = 0.15f),
                            CyberPurple.copy(alpha = 0.1f)
                        )
                    )
                )
                .border(1.dp, ElectricBlue.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Monthly Spending Planner", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    Box(
                        modifier = Modifier
                            .background(healthColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(healthStatus, color = healthColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Money Flow Visualization
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MoneyFlowStep(label = "Income", amount = budget.monthlyBudget, color = ElectricBlue)
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(16.dp))
                    MoneyFlowStep(label = "Saved", amount = budget.savingsGoal, color = CyberPurple)
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(16.dp))
                    MoneyFlowStep(label = "Available", amount = budget.wishlistSpendingBudget, color = SolarYellow)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Remaining to Spend", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("₹", color = MintGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                    AnimatedNumber(
                        targetValue = budget.remainingAvailable.toInt(),
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = MintGreen
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                VisualBudgetBar(budget = budget)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SimpleBudgetStat(label = "Spent This Month", value = budget.spentThisMonth, color = Color(0xFFFF8C00))
                    SimpleBudgetStat(label = "Active Wishes", value = activeItemsCount.toDouble(), color = Color.White, isCount = true)
                }
            }
        }
    }
}

@Composable
fun MoneyFlowStep(label: String, amount: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
        Text("₹${amount.toInt()}", color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun VisualBudgetBar(budget: BudgetData) {
    val total = budget.monthlyBudget.coerceAtLeast(1.0)
    val savingsWeight = (budget.savingsGoal / total).toFloat()
    val spentWeight = (budget.spentThisMonth / total).toFloat()
    val remainingWeight = (budget.remainingAvailable / total).toFloat()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            // Savings (Blue/Purple)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(savingsWeight.coerceAtLeast(0.01f))
                    .background(CyberPurple)
            )
            // Spent (Orange/Red)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(spentWeight.coerceAtLeast(0.01f))
                    .background(Color(0xFFFF8C00))
            )
            // Remaining (Green)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(remainingWeight.coerceAtLeast(0.01f))
                    .background(MintGreen)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            LegendItem(label = "Saved", color = CyberPurple)
            LegendItem(label = "Used", color = Color(0xFFFF8C00))
            LegendItem(label = "Left", color = MintGreen)
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
    }
}

@Composable
fun SimpleBudgetStat(label: String, value: Double, color: Color, isCount: Boolean = false) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
        if (isCount) {
            Text(value.toInt().toString(), color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        } else {
            Text("₹${value.toInt()}", color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun QuickAddWishButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Add New Wish", fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PrioritySectionHeader(priority: BuyPriority) {
    Text(
        text = priority.displayName,
        color = Color(priority.colorHex),
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun SimplifiedWishlistCard(
    item: BuyItem,
    budget: BudgetData,
    onClick: () -> Unit,
    onQuickSave: (Double) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onArchive: () -> Unit,
    onMarkAsPurchased: () -> Unit
) {
    val progress = (item.amountSaved / item.price).coerceIn(0.0, 1.0).toFloat()
    val remainingToSave = (item.price - item.amountSaved).coerceAtLeast(0.0)
    val priorityColor = Color(item.priority.colorHex)
    var showMenu by remember { mutableStateOf(false) }
    
    val affordabilityStatus = when {
        item.price <= budget.remainingAvailable -> "✅ Affordable"
        item.price <= budget.remainingAvailable + (budget.monthlyBudget * 0.1) -> "⚠ Tight Budget"
        else -> "❌ Over Budget"
    }
    
    val statusColor = when {
        item.price <= budget.remainingAvailable -> MintGreen
        item.price <= budget.remainingAvailable + (budget.monthlyBudget * 0.1) -> SolarYellow
        else -> Color(0xFFFF3B30)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, priorityColor.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = DeepVoid.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(priorityColor.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = priorityColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("₹${item.price.toInt()}", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White.copy(alpha = 0.5f))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(DeepVoid).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit", color = Color.White) },
                            onClick = { showMenu = false; onEdit() },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = ElectricBlue) }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate", color = Color.White) },
                            onClick = { showMenu = false; onDuplicate() },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = SolarYellow) }
                        )
                        DropdownMenuItem(
                            text = { Text("Archive", color = Color.White) },
                            onClick = { showMenu = false; onArchive() },
                            leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null, tint = CyberPurple) }
                        )
                        DropdownMenuItem(
                            text = { Text("Mark as Purchased", color = Color.White) },
                            onClick = { showMenu = false; onMarkAsPurchased() },
                            leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MintGreen) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFFF3B30)) },
                            onClick = { showMenu = false; onDelete() },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF3B30)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(affordabilityStatus, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                if (item.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(item.notes, color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (progress >= 1f) MintGreen else priorityColor,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("${(progress * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Saved: ₹${item.amountSaved.toInt()}", color = MintGreen, fontSize = 11.sp)
                Text("Need: ₹${remainingToSave.toInt()}", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Save Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickSaveChip(label = "+₹50", onClick = { onQuickSave(50.0) })
                QuickSaveChip(label = "+₹500", onClick = { onQuickSave(500.0) })
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                        .clickable { onClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Details", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun QuickSaveChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .padding(horizontal = 4.dp)
            .background(ElectricBlue.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = ElectricBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun SavingsProgressSummary(totalSaved: Double, totalGoal: Double) {
    val overallProgress = if (totalGoal > 0) (totalSaved / totalGoal).toFloat() else 0f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MintGreen.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                .border(1.dp, MintGreen.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Overall Progress", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White.copy(alpha = 0.05f),
                    strokeWidth = 8.dp
                )
                CircularProgressIndicator(
                    progress = { overallProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = MintGreen,
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${(overallProgress * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "You've saved ${CurrencyUtils.formatIndianRupees(totalSaved)} of ${CurrencyUtils.formatIndianRupees(totalGoal)}",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ThisMonthPurchasesHeader(count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("This Month Purchases 🛍️", color = MintGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("$count Items", color = MintGreen.copy(alpha = 0.6f), fontSize = 12.sp)
    }
}

@Composable
fun AchievedGoalCard(item: BuyItem, onUndo: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MintGreen.copy(alpha = 0.05f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MintGreen, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Medium)
                Text("Bought on ${item.purchasedDate ?: "Recently"}", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
            }
            IconButton(onClick = onUndo) {
                Icon(Icons.Default.Undo, contentDescription = "Undo", tint = Color.White.copy(alpha = 0.5f))
            }
            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = SolarYellow, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun ArchivedHeader(count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Archived Dreams 📁", color = CyberPurple, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("$count Items", color = CyberPurple.copy(alpha = 0.6f), fontSize = 12.sp)
    }
}

@Composable
fun ArchivedItemCard(item: BuyItem, onUnarchive: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DeepVoid.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Archive, contentDescription = null, tint = CyberPurple.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Medium)
                Text("₹${item.price.toInt()}", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp)
            }
            IconButton(onClick = onUnarchive) {
                Icon(Icons.Default.Unarchive, contentDescription = "Unarchive", tint = ElectricBlue)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF3B30).copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun RecentlyDeletedHeader(count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Recently Deleted 🗑️", color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("$count Items", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp)
    }
}

@Composable
fun DeletedItemCard(item: BuyItem, onRestore: () -> Unit, onPermanentDelete: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            containerColor = DeepVoid,
            title = { Text("Permanent Delete?", color = Color.White) },
            text = { Text("This action cannot be undone.", color = Color.White.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(onClick = onPermanentDelete) { Text("Delete Forever", color = Color(0xFFFF3B30)) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Cancel", color = Color.White) }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.02f)).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, color = Color.White.copy(alpha = 0.3f), textDecoration = TextDecoration.LineThrough)
                Text("Deleted on ${item.deletedDate}", color = Color.White.copy(alpha = 0.2f), fontSize = 10.sp)
            }
            IconButton(onClick = onRestore) {
                Icon(Icons.Default.Restore, contentDescription = "Restore", tint = MintGreen)
            }
            IconButton(onClick = { showConfirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Permanent Delete", tint = Color(0xFFFF3B30))
            }
        }
    }
}

@Composable
fun SimpleAddWishDialog(onDismiss: () -> Unit, onAdd: (BuyItem) -> Unit) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(BuyPriority.IMPORTANT) }
    var showMoreOptions by remember { mutableStateOf(false) }
    
    // More options state
    var saved by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepVoid,
        shape = RoundedCornerShape(28.dp),
        title = { Text("New Wish ✨", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("What are you dreaming of?") },
                    placeholder = { Text("e.g. New Headphones") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (₹)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Priority", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BuyPriority.values().forEach { p ->
                        PriorityChip(priority = p, isSelected = priority == p, onClick = { priority = p })
                    }
                }

                TextButton(
                    onClick = { showMoreOptions = !showMoreOptions },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (showMoreOptions) "Show Less" else "More Details", color = ElectricBlue.copy(alpha = 0.7f))
                }

                AnimatedVisibility(visible = showMoreOptions) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = saved,
                            onValueChange = { saved = it },
                            label = { Text("Already Saved (₹)") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes (optional)") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = link,
                            onValueChange = { link = it },
                            label = { Text("Product Link") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = ElectricBlue) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(BuyItem(
                        name = name,
                        price = price.toDoubleOrNull() ?: 0.0,
                        amountSaved = saved.toDoubleOrNull() ?: 0.0,
                        priority = priority,
                        notes = notes,
                        link = link
                    ))
                },
                enabled = name.isNotBlank() && price.toDoubleOrNull() != null,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add Wish", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun PriorityChip(priority: BuyPriority, isSelected: Boolean, onClick: () -> Unit) {
    val color = Color(priority.colorHex)
    Box(
        modifier = Modifier
            .height(40.dp)
            .background(
                if (isSelected) color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(10.dp)
            )
            .border(
                if (isSelected) 1.dp else 0.dp,
                if (isSelected) color else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            priority.displayName.split(" ").last(), // Just "Must", "Important", etc.
            color = if (isSelected) color else Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun SimplifiedBudgetSetupDialog(budget: BudgetData, onDismiss: () -> Unit, onSave: (BudgetData) -> Unit) {
    var monthlyBudget by remember { mutableStateOf(budget.monthlyBudget.toString()) }
    var savingsGoal by remember { mutableStateOf(budget.savingsGoal.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepVoid,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Budget Settings", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = monthlyBudget,
                    onValueChange = { monthlyBudget = it },
                    label = { Text("Monthly Income (₹)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = savingsGoal,
                    onValueChange = { savingsGoal = it },
                    label = { Text("General Savings Goal (₹)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
                Text(
                    "Available for Wishlist: ₹${(monthlyBudget.toDoubleOrNull() ?: 0.0) - (savingsGoal.toDoubleOrNull() ?: 0.0)}",
                    color = SolarYellow,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(budget.copy(
                    monthlyBudget = monthlyBudget.toDoubleOrNull() ?: 0.0,
                    savingsGoal = savingsGoal.toDoubleOrNull() ?: 0.0
                ))
            }, colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)) {
                Text("Save Changes")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimplifiedItemDetailSheet(
    item: BuyItem,
    budget: BudgetData,
    onDismiss: () -> Unit,
    onPurchase: () -> Unit,
    onDelete: () -> Unit,
    onUpdateItem: (BuyItem) -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var price by remember { mutableStateOf(item.price.toString()) }
    var saved by remember { mutableStateOf(item.amountSaved.toString()) }
    var priority by remember { mutableStateOf(item.priority) }
    var notes by remember { mutableStateOf(item.notes) }
    var link by remember { mutableStateOf(item.link) }
    
    val modalState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalState,
        containerColor = DeepVoid,
        dragHandle = { Box(modifier = Modifier.padding(top = 12.dp).size(40.dp, 4.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Edit Wish ✨", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (₹)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = saved,
                    onValueChange = { saved = it },
                    label = { Text("Saved (₹)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Text("Priority", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BuyPriority.values().forEach { p ->
                    PriorityChip(priority = p, isSelected = priority == p, onClick = { priority = p })
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("Product Link") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = ElectricBlue) }
                )
                
                if (link.isNotBlank() && (link.startsWith("http") || link.contains("."))) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ElectricBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .border(1.dp, ElectricBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable { /* Link click would happen here if we had URI opener */ }
                            .padding(12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Link Preview", color = ElectricBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    onUpdateItem(item.copy(
                        name = name,
                        price = price.toDoubleOrNull() ?: item.price,
                        amountSaved = saved.toDoubleOrNull() ?: item.amountSaved,
                        priority = priority,
                        notes = notes,
                        link = link
                    ))
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            var showConfirmation by remember { mutableStateOf(false) }
            if (showConfirmation) {
                PurchaseConfirmationUI(price = item.price.toInt(), onDismiss = onDismiss)
            } else {
                Button(
                    onClick = { onPurchase(); showConfirmation = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (item.price <= budget.remainingAvailable) MintGreen else Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp),
                    enabled = item.price <= budget.remainingAvailable
                ) {
                    Text("Mark as Purchased 🛍️", fontWeight = FontWeight.Bold)
                }
            }
            
            TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                Text("Delete Wish", color = Color(0xFFFF3B30))
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PurchaseConfirmationUI(price: Int, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MintGreen, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Purchase Confirmed!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("₹$price deducted from your available budget.", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Awesome!")
        }
    }
}

@Composable
fun SimplifiedEmptyState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(120.dp).background(ElectricBlue.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(60.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Your dream board is empty", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Start by adding something you really want!", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}

// Re-using some simple util-like components or adding missing ones
@Composable
fun AchievedGoalBadge() {
    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MintGreen, modifier = Modifier.size(16.dp))
}