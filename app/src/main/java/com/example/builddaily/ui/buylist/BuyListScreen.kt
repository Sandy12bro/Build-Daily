package com.example.builddaily.ui.buylist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.builddaily.data.model.BuyCategory
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
    val selectedTab by viewModel.selectedTab.collectAsState()
    
    val activeItems by viewModel.activeItems.collectAsState()
    val purchasedItems by viewModel.purchasedItems.collectAsState()
    val filteredItems by viewModel.filteredItems.collectAsState()
    
    val totalWishlistValue by viewModel.totalWishlistValue.collectAsState(0.0)
    val totalSaved by viewModel.totalSaved.collectAsState(0.0)
    val purchasedValue by viewModel.purchasedValue.collectAsState(0.0)

    val affordableCount = activeItems.count { it.price <= budget.currentSavings }
    val canAffordItems = activeItems.filter { it.price <= budget.currentSavings }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<BuyItem?>(null) }
    var showBudgetDialog by remember { mutableStateOf(false) }

    val motivationalMessages = listOf(
        "Your future setup starts here ✨",
        "Small savings build big dreams 🌟",
        "One step closer to your dream setup 🚀",
        "Every rupee counts towards your goal 💰",
        "Future you will thank present you 🎯"
    )
    var currentMessage by remember { mutableStateOf(motivationalMessages.random()) }

    LaunchedEffect(activeItems.size) {
        if (activeItems.isNotEmpty()) {
            currentMessage = motivationalMessages.random()
        }
    }

    Scaffold(
        containerColor = DeepVoid,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Wishlist Planner", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ElectricBlue
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add item", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                PremiumBudgetOverviewCard(
                    budget = budget,
                    totalWishlistValue = totalWishlistValue,
                    purchasedValue = purchasedValue,
                    totalSaved = totalSaved,
                    affordableCount = affordableCount
                )
            }

            item {
                PremiumSavingsGoalCard(
                    budget = budget,
                    items = activeItems
                )
            }

            item {
                AnimatedVisibility(
                    visible = activeItems.isEmpty(),
                    enter = fadeIn() + slideInVertically()
                ) {
                    PremiumEmptyState(
                        title = currentMessage,
                        subtitle = "Add items you want to buy, set priorities, and track your savings journey"
                    )
                }
            }

            if (activeItems.isNotEmpty()) {
                item {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = ElectricBlue,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = ElectricBlue
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { viewModel.setTab(0) },
                            text = { Text("All (${activeItems.size})", color = if (selectedTab == 0) ElectricBlue else Color.White.copy(alpha = 0.6f)) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { viewModel.setTab(1) },
                            text = { Text("Can Afford (${canAffordItems.size})", color = if (selectedTab == 1) MintGreen else Color.White.copy(alpha = 0.6f)) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { viewModel.setTab(2) },
                            text = { Text("Saving (${activeItems.count { it.itemStatus == ItemStatus.SAVING_FOR }})", color = if (selectedTab == 2) SolarYellow else Color.White.copy(alpha = 0.6f)) }
                        )
                    }
                }

                val filteredItems = when (selectedTab) {
                    0 -> activeItems
                    1 -> canAffordItems
                    2 -> activeItems.filter { it.itemStatus == ItemStatus.SAVING_FOR }
                    else -> activeItems
                }

                item {
                    Text(
                        "Your Wishlist",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(filteredItems, key = { it.id }) { item ->
                    PremiumBuyItemCard(
                        item = item,
                        budget = budget,
                        onClick = { selectedItem = item },
                        onPurchase = { viewModel.markAsPurchased(item.id) },
                        onDelete = { viewModel.deleteItem(item.id) },
                        onUpdateSaved = { newAmount -> viewModel.updateItem(item.copy(amountSaved = newAmount)) }
                    )
                }
            }

            if (purchasedItems.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    PurchasedSectionHeader(count = purchasedItems.size, totalValue = purchasedValue)
                }

                items(purchasedItems.take(10), key = { "purchased_${it.id}" }) { item ->
                    PremiumPurchasedItemCard(item = item, onDelete = { viewModel.deleteItem(item.id) })
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }

    if (showAddDialog) {
        PremiumAddBuyItemDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { item ->
                viewModel.addItem(item)
                showAddDialog = false
            }
        )
    }

    if (showBudgetDialog) {
        PremiumBudgetSetupDialog(
            budget = budget,
            onDismiss = { showBudgetDialog = false },
            onSave = { newBudget ->
                viewModel.updateBudget(newBudget)
                showBudgetDialog = false
            }
        )
    }

    selectedItem?.let { item ->
        PremiumItemDetailSheet(
            item = item,
            budget = budget,
            onDismiss = { selectedItem = null },
            onPurchase = { viewModel.markAsPurchased(item.id) },
            onDelete = { viewModel.deleteItem(item.id); selectedItem = null },
            onUpdateSaved = { newAmount -> viewModel.updateItem(item.copy(amountSaved = newAmount)); selectedItem = item.copy(amountSaved = newAmount) }
        )
    }
}

@Composable
fun PremiumBudgetOverviewCard(
    budget: BudgetData,
    totalWishlistValue: Double,
    purchasedValue: Double,
    totalSaved: Double,
    affordableCount: Int
) {
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
                .border(1.dp, ElectricBlue.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Financial Overview", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = SolarYellow, modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AnimatedStatBox(
                        label = "Monthly Budget",
                        value = budget.monthlyBudget,
                        color = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AnimatedStatBox(
                        label = "Savings",
                        value = budget.currentSavings,
                        color = MintGreen,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AnimatedStatBox(
                        label = "Goal",
                        value = budget.savingsGoal,
                        color = SolarYellow,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MiniStat(label = "Wishlist", value = totalWishlistValue, color = CyberPurple)
                    MiniStat(label = "Purchased", value = purchasedValue, color = MintGreen)
                    MiniStat(label = "Can Afford", value = affordableCount.toDouble(), color = ElectricBlue, isCount = true)
                }
            }
        }
    }
}

@Composable
fun AnimatedStatBox(label: String, value: Double, color: Color, modifier: Modifier = Modifier) {
    var animatedValue by remember { mutableFloatStateOf(0f) }
    val progress = remember { mutableFloatStateOf(0f) }

    LaunchedEffect(value) {
        progress.value = (value / 100000.0).coerceIn(0.0, 1.0).toFloat()
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedCurrency(
                targetValue = value,
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
        }
    }
}

@Composable
fun MiniStat(label: String, value: Double, color: Color, isCount: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (isCount) {
            AnimatedNumber(
                targetValue = value.toInt(),
                style = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            )
        } else {
            AnimatedCurrency(
                targetValue = value,
                style = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            )
        }
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
    }
}

@Composable
fun PremiumSavingsGoalCard(budget: BudgetData, items: List<BuyItem>) {
    val progress = (budget.currentSavings / budget.savingsGoal).coerceIn(0.0, 1.0).toFloat()
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(1000), label = "savings")

    val monthlySavings = budget.currentSavings
    val daysInMonth = 30

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.horizontalGradient(listOf(MintGreen.copy(alpha = 0.1f), ElectricBlue.copy(alpha = 0.05f))))
                .border(1.dp, MintGreen.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Savings, contentDescription = null, tint = MintGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Savings Goal Progress", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    AnimatedPercentage(targetValue = progress.toFloat(), style = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MintGreen))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                brush = Brush.horizontalGradient(listOf(MintGreen, ElectricBlue))
                            )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        CurrencyUtils.formatIndianRupees(budget.currentSavings),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Text(
                        "of ${CurrencyUtils.formatIndianRupees(budget.savingsGoal)}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }

                if (items.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MintGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = MintGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        val totalRemaining = items.sumOf { it.price - it.amountSaved }
                        val avgDailySave = CurrencyUtils.calculateDailySaving(totalRemaining, monthlySavings, daysInMonth)
                        Text(
                            "Save ₹${CurrencyUtils.formatIndianRupees(avgDailySave).replace("₹", "").trim()}/day to reach all goals",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumBuyItemCard(
    item: BuyItem,
    budget: BudgetData,
    onClick: () -> Unit,
    onPurchase: () -> Unit,
    onDelete: () -> Unit,
    onUpdateSaved: (Double) -> Unit
) {
    var showSaveDialog by remember { mutableStateOf(false) }

    val priorityColor = Color(item.priority.colorHex)
    val canAfford = item.price <= budget.currentSavings
    val savingsProgress = (item.amountSaved / item.price).coerceIn(0.0, 1.0).toFloat()
    val remainingAmount = item.price - item.amountSaved

    val daysToAfford = if (budget.monthlySavingsRate > 0) {
        (remainingAmount / budget.monthlySavingsRate * 30).toInt()
    } else 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, priorityColor.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            priorityColor.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(priorityColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            item.category.emoji,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                item.name.ifEmpty { "Unnamed Item" },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            PriorityBadge(priority = item.priority, color = priorityColor)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AnimatedCurrency(
                                targetValue = item.price,
                                style = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = if (canAfford) MintGreen else Color.White)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusChip(status = item.itemStatus, canAfford = canAfford)
                        }
                    }
                }

                if (item.itemStatus == ItemStatus.SAVING_FOR || item.amountSaved > 0) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Saved: ${CurrencyUtils.formatIndianRupees(item.amountSaved)}", color = MintGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text("Remaining: ${CurrencyUtils.formatIndianRupees(remainingAmount)}", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val animatedSavingsProgress by animateFloatAsState(targetValue = savingsProgress, animationSpec = tween(800), label = "savings")
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animatedSavingsProgress)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MintGreen)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        IconButton(
                            onClick = { showSaveDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MintGreen.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add savings", tint = MintGreen, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        if (item.deadline != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = SolarYellow, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(item.deadline, color = SolarYellow, fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(item.category.displayName, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }

                    Row {
                        if (!canAfford && daysToAfford > 0 && daysToAfford < 365) {
                            Text(
                                "≈ ${daysToAfford} days",
                                color = SolarYellow,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .background(SolarYellow.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        IconButton(
                            onClick = onPurchase,
                            modifier = Modifier
                                .size(32.dp)
                                .background(if (canAfford) MintGreen.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Mark as purchased", tint = if (canAfford) MintGreen else Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        AddSavingsDialog(
            currentSaved = item.amountSaved,
            targetPrice = item.price,
            onDismiss = { showSaveDialog = false },
            onSave = { amount ->
                onUpdateSaved(item.amountSaved + amount)
                showSaveDialog = false
            }
        )
    }
}

@Composable
fun PriorityBadge(priority: BuyPriority, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Flag, contentDescription = null, tint = color, modifier = Modifier.size(10.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(priority.displayName, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatusChip(status: ItemStatus, canAfford: Boolean) {
    val (color, icon) = when (status) {
        ItemStatus.PLANNED -> Color.White.copy(alpha = 0.5f) to Icons.Default.CalendarMonth
        ItemStatus.SAVING_FOR -> SolarYellow to Icons.Default.Savings
        ItemStatus.CAN_AFFORD -> MintGreen to Icons.Default.CheckCircle
        ItemStatus.PURCHASED -> MintGreen to Icons.Default.Check
        ItemStatus.POSTPONED -> Color.Gray to Icons.Default.Timer
    }

    val bgColor = when {
        canAfford && status != ItemStatus.PURCHASED -> MintGreen.copy(alpha = 0.15f)
        else -> color.copy(alpha = 0.1f)
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(10.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(status.displayName, color = color, fontSize = 9.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun PremiumEmptyState(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(ElectricBlue.copy(alpha = 0.3f), Color.Transparent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(ElectricBlue.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(35.dp))
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun PurchasedSectionHeader(count: Int, totalValue: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MintGreen.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MintGreen, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text("Purchased Items ($count)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Text(CurrencyUtils.formatIndianRupees(totalValue), color = MintGreen, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
    }
}

@Composable
fun PremiumPurchasedItemCard(item: BuyItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MintGreen.copy(alpha = 0.03f))
                .border(1.dp, MintGreen.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MintGreen.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = MintGreen, modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Medium, maxLines = 1)
                    Text("Purchased: ${item.purchasedDate ?: "Recently"}", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                }

                Text(
                    CurrencyUtils.formatIndianRupees(item.price),
                    color = MintGreen.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun AddSavingsDialog(currentSaved: Double, targetPrice: Double, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepVoid,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Savings, contentDescription = null, tint = MintGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Savings", color = Color.White)
            }
        },
        text = {
            Column {
                Text("Current: ${CurrencyUtils.formatIndianRupees(currentSaved)} / ${CurrencyUtils.formatIndianRupees(targetPrice)}", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = MintGreen) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amount.toDoubleOrNull()?.let { onSave(it) }
                },
                enabled = amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0
            ) {
                Text("Add", color = MintGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.5f))
            }
        }
    )
}

@Composable
fun PremiumBudgetSetupDialog(budget: BudgetData, onDismiss: () -> Unit, onSave: (BudgetData) -> Unit) {
    var monthlyBudget by remember { mutableStateOf(budget.monthlyBudget.toString()) }
    var savingsGoal by remember { mutableStateOf(budget.savingsGoal.toString()) }
    var currentSavings by remember { mutableStateOf(budget.currentSavings.toString()) }
    var essentialReserve by remember { mutableStateOf(budget.essentialReserve.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepVoid,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = ElectricBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Budget Setup", color = Color.White)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = monthlyBudget,
                    onValueChange = { monthlyBudget = it },
                    label = { Text("Monthly Budget (₹)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = ElectricBlue) }
                )
                OutlinedTextField(
                    value = savingsGoal,
                    onValueChange = { savingsGoal = it },
                    label = { Text("Monthly Savings Goal (₹)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Savings, contentDescription = null, tint = MintGreen) }
                )
                OutlinedTextField(
                    value = currentSavings,
                    onValueChange = { currentSavings = it },
                    label = { Text("Current Savings (₹)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SolarYellow, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Savings, contentDescription = null, tint = SolarYellow) }
                )
                OutlinedTextField(
                    value = essentialReserve,
                    onValueChange = { essentialReserve = it },
                    label = { Text("Essential Reserve (₹)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberPurple, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CyberPurple) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        BudgetData(
                            monthlyBudget = monthlyBudget.toDoubleOrNull() ?: 0.0,
                            savingsGoal = savingsGoal.toDoubleOrNull() ?: 0.0,
                            currentSavings = currentSavings.toDoubleOrNull() ?: 0.0,
                            essentialReserve = essentialReserve.toDoubleOrNull() ?: 0.0
                        )
                    )
                }
            ) {
                Text("Save", color = ElectricBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.5f))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumItemDetailSheet(
    item: BuyItem,
    budget: BudgetData,
    onDismiss: () -> Unit,
    onPurchase: () -> Unit,
    onDelete: () -> Unit,
    onUpdateSaved: (Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepVoid,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.category.emoji,
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(item.name.ifEmpty { "Item" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(item.category.displayName, color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF3B30))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DetailStat(label = "Price", value = CurrencyUtils.formatIndianRupees(item.price), color = ElectricBlue)
                DetailStat(label = "Saved", value = CurrencyUtils.formatIndianRupees(item.amountSaved), color = MintGreen)
                DetailStat(label = "Remaining", value = CurrencyUtils.formatIndianRupees(item.price - item.amountSaved), color = SolarYellow)
            }

            Spacer(modifier = Modifier.height(20.dp))

            val progress = (item.amountSaved / item.price).coerceIn(0.0, 1.0).toFloat()
            val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(800), label = "progress")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(brush = Brush.horizontalGradient(listOf(MintGreen, ElectricBlue)))
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "${(progress * 100).toInt()}% saved",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            if (item.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text("Notes", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(item.notes, color = Color.White, fontSize = 14.sp)
            }

            if (item.link.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Link attached", color = ElectricBlue, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onPurchase,
                    modifier = Modifier
                        .weight(1f)
                        .background(MintGreen.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(vertical = 12.dp),
                    enabled = item.price <= budget.currentSavings
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = if (item.price <= budget.currentSavings) MintGreen else Color.White.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mark Purchased", color = if (item.price <= budget.currentSavings) MintGreen else Color.White.copy(alpha = 0.3f))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DetailStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
    }
}

@Composable
fun PremiumAddBuyItemDialog(onDismiss: () -> Unit, onAdd: (BuyItem) -> Unit) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(BuyCategory.OTHER) }
    var priority by remember { mutableStateOf(BuyPriority.IMPORTANT) }
    var notes by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var itemStatus by remember { mutableStateOf(ItemStatus.PLANNED) }
    var amountSaved by remember { mutableStateOf("0") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showPriorityDropdown by remember { mutableStateOf(false) }
    var showStatusDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepVoid,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = ElectricBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Wishlist Item", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price (₹)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = ElectricBlue) }
                    )
                }
                item {
                    OutlinedTextField(
                        value = category.displayName,
                        onValueChange = {},
                        label = { Text("Category") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showCategoryDropdown = true }) {
                                Icon(Icons.Default.Category, contentDescription = null, tint = ElectricBlue)
                            }
                        }
                    )
                }
                item {
                    OutlinedTextField(
                        value = priority.displayName,
                        onValueChange = {},
                        label = { Text("Priority") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showPriorityDropdown = true }) {
                                Icon(Icons.Default.Flag, contentDescription = null, tint = ElectricBlue)
                            }
                        }
                    )
                }
                item {
                    OutlinedTextField(
                        value = itemStatus.displayName,
                        onValueChange = {},
                        label = { Text("Status") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showStatusDropdown = true }) {
                                Icon(Icons.Default.Flag, contentDescription = null, tint = ElectricBlue)
                            }
                        }
                    )
                }
                item {
                    OutlinedTextField(
                        value = amountSaved,
                        onValueChange = { amountSaved = it },
                        label = { Text("Amount Already Saved (₹)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Savings, contentDescription = null, tint = MintGreen) }
                    )
                }
                item {
                    OutlinedTextField(
                        value = deadline,
                        onValueChange = { deadline = it },
                        label = { Text("Deadline (YYYY-MM-DD)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SolarYellow, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = SolarYellow) }
                    )
                }
                item {
                    OutlinedTextField(
                        value = link,
                        onValueChange = { link = it },
                        label = { Text("Product Link (optional)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = ElectricBlue) }
                    )
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newItem = BuyItem(
                        name = name,
                        price = price.toDoubleOrNull() ?: 0.0,
                        category = category,
                        priority = priority,
                        notes = notes,
                        deadline = deadline.ifEmpty { null },
                        link = link,
                        itemStatus = itemStatus,
                        amountSaved = amountSaved.toDoubleOrNull() ?: 0.0
                    )
                    onAdd(newItem)
                },
                enabled = name.isNotBlank() && price.toDoubleOrNull() != null
            ) {
                Text("Add Item", color = ElectricBlue, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.5f))
            }
        }
    )

    if (showCategoryDropdown) {
        CategorySelectionDialog(
            categories = BuyCategory.entries.toList(),
            selected = category,
            onSelect = {
                category = it
                showCategoryDropdown = false
            },
            onDismiss = { showCategoryDropdown = false }
        )
    }

    if (showPriorityDropdown) {
        PrioritySelectionDialog(
            priorities = BuyPriority.entries.toList(),
            selected = priority,
            onSelect = {
                priority = it
                showPriorityDropdown = false
            },
            onDismiss = { showPriorityDropdown = false }
        )
    }

    if (showStatusDropdown) {
        StatusSelectionDialog(
            statuses = ItemStatus.entries.toList(),
            selected = itemStatus,
            onSelect = {
                itemStatus = it
                showStatusDropdown = false
            },
            onDismiss = { showStatusDropdown = false }
        )
    }
}

@Composable
fun CategorySelectionDialog(
    categories: List<BuyCategory>,
    selected: BuyCategory,
    onSelect: (BuyCategory) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepVoid,
        title = { Text("Select Category", color = Color.White) },
        text = {
            LazyColumn {
                items(categories.size) { index ->
                    val category = categories[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(category) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(category.emoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            category.displayName,
                            color = if (category == selected) ElectricBlue else Color.White,
                            fontWeight = if (category == selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.White.copy(alpha = 0.5f)) }
        }
    )
}

@Composable
fun PrioritySelectionDialog(
    priorities: List<BuyPriority>,
    selected: BuyPriority,
    onSelect: (BuyPriority) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepVoid,
        title = { Text("Select Priority", color = Color.White) },
        text = {
            LazyColumn {
                items(priorities.size) { index ->
                    val priority = priorities[index]
                    val color = Color(priority.colorHex)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(priority) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(color, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            priority.displayName,
                            color = if (priority == selected) color else Color.White,
                            fontWeight = if (priority == selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.White.copy(alpha = 0.5f)) }
        }
    )
}

@Composable
fun StatusSelectionDialog(
    statuses: List<ItemStatus>,
    selected: ItemStatus,
    onSelect: (ItemStatus) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepVoid,
        title = { Text("Select Status", color = Color.White) },
        text = {
            LazyColumn {
                items(statuses.size) { index ->
                    val status = statuses[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(status) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            status.displayName,
                            color = if (status == selected) ElectricBlue else Color.White,
                            fontWeight = if (status == selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.White.copy(alpha = 0.5f)) }
        }
    )
}