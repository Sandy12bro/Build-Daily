package com.example.builddaily.ui.booklibrary

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.datetime.Clock
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.builddaily.data.model.Book
import com.example.builddaily.data.model.BookPriority
import com.example.builddaily.data.model.ReadingStatus
import com.example.builddaily.data.repository.ReadingGoal
import com.example.builddaily.ui.theme.*
import com.example.builddaily.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookLibraryScreen(
    onBack: () -> Unit,
    viewModel: BookLibraryViewModel
) {
    val books by viewModel.books.collectAsState()
    val readingGoal by viewModel.readingGoal.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    
    val currentlyReading by viewModel.currentlyReading.collectAsState()
    val toRead by viewModel.toRead.collectAsState()
    val completed by viewModel.completed.collectAsState()
    val favouriteStatusBooks by viewModel.favouriteStatusBooks.collectAsState()
    val totalPagesRead by viewModel.totalPagesRead.collectAsState(0)
    val completedCount by viewModel.completedCount.collectAsState(0)

    var showAddBookDialog by remember { mutableStateOf(false) }
    var selectedBook by remember { mutableStateOf<Book?>(null) }
    var showAnalytics by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    val sortOption by viewModel.sortOption.collectAsState()

    Scaffold(
        containerColor = DeepVoid,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Column {
                        Text("Reading Vault", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text("Level up your mind 🧠", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort", tint = CyberPurple)
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            containerColor = DeepVoid
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.displayName, color = if (sortOption == option) CyberPurple else Color.White) },
                                    onClick = {
                                        viewModel.setSortOption(option)
                                        showSortMenu = false
                                    },
                                    leadingIcon = { if (sortOption == option) Icon(Icons.Default.Check, contentDescription = null, tint = CyberPurple, modifier = Modifier.size(16.dp)) }
                                )
                            }
                        }
                    }
                    IconButton(onClick = { showAnalytics = !showAnalytics }) {
                        Icon(Icons.Default.BarChart, contentDescription = "Analytics", tint = CyberPurple)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddBookDialog = true },
                containerColor = CyberPurple,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Book", tint = Color.White)
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

            // Analytics Section (Expandable)
            if (showAnalytics) {
                item {
                    ReadingAnalyticsCard(
                        completedCount = completedCount,
                        totalReadPages = totalPagesRead,
                        totalBooks = books.size,
                        yearlyGoal = readingGoal.yearlyGoal
                    )
                }
            }

            if (showAnalytics) {
                item {
                    ReadingGoalProgressCard(
                        readingGoal = readingGoal,
                        booksReadThisYear = completedCount,
                        onEdit = { showGoalDialog = true }
                    )
                }
            }

            // Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CyberPurple
                        )
                    }
                ) {
                    val tabData = listOf(
                        "Reading" to currentlyReading.size,
                        "Want" to toRead.size,
                        "Done" to completed.size,
                        "Fav" to favouriteStatusBooks.size
                    )

                    tabData.forEachIndexed { index, (title, count) ->
                        val isSelected = selectedTab == index
                        Tab(
                            selected = isSelected,
                            onClick = { viewModel.setTab(index) },
                            text = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        title,
                                        color = if (isSelected) CyberPurple else Color.White.copy(alpha = 0.6f),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        "($count)",
                                        color = if (isSelected) CyberPurple else Color.White.copy(alpha = 0.4f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        )
                    }
                }
            }

            val displayedBooks = when (selectedTab) {
                0 -> currentlyReading
                1 -> toRead
                2 -> completed
                3 -> favouriteStatusBooks
                else -> currentlyReading
            }

            if (displayedBooks.isEmpty()) {
                item {
                    ReadingEmptyState(tab = selectedTab)
                }
            } else {
                items(displayedBooks, key = { it.id }) { book ->
                    ModernBookCard(
                        book = book,
                        onEdit = { selectedBook = book },
                        onToggleFavorite = { viewModel.updateBook(book.copy(isFavorite = !book.isFavorite)) },
                        onAdjustPages = { delta -> viewModel.adjustPages(book.id, delta) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }

        if (showGoalDialog) {
            EditReadingGoalDialog(
                currentGoal = readingGoal,
                onDismiss = { showGoalDialog = false },
                onSave = { updatedGoal ->
                    viewModel.updateReadingGoal(updatedGoal)
                    showGoalDialog = false
                }
            )
        }
    }

    if (showAddBookDialog) {
        AddBookDialog(
            onDismiss = { showAddBookDialog = false },
            onAdd = { book ->
                viewModel.addBook(book)
                showAddBookDialog = false
            }
        )
    }

    if (selectedBook != null) {
        EditBookDialog(
            book = selectedBook!!,
            onDismiss = { selectedBook = null },
            onUpdate = { updatedBook ->
                viewModel.updateBook(updatedBook)
                selectedBook = null
            },
            onDelete = {
                viewModel.deleteBook(selectedBook!!.id)
                selectedBook = null
            }
        )
    }
}

@Composable
fun CustomTabRow(selectedTab: Int, onTabSelected: (Int) -> Unit, tabs: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) CyberPurple else Color.Transparent)
                    .clickable { onTabSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    title,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ModernBookCard(
    book: Book,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAdjustPages: (Int) -> Unit
) {
    val priorityColor = Color(book.priority.colorHex)
    val progress = book.progress
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(1000), label = "bookProgress")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .border(1.dp, priorityColor.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = DeepVoid.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                // Book Cover
                Box(
                    modifier = Modifier
                        .size(60.dp, 80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(priorityColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (book.coverUri != null) {
                        AsyncImage(
                            model = book.coverUri,
                            contentDescription = "Cover for ${book.title}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Book, contentDescription = null, tint = priorityColor.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            book.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .background(priorityColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(book.priority.displayName.split(" ").last(), color = priorityColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Text(book.author.ifEmpty { "Unknown Author" }, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    
                    if (book.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(book.tags) { tag ->
                                Text(
                                    "#$tag",
                                    color = ElectricBlue.copy(alpha = 0.6f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (progress >= 1f) MintGreen else priorityColor,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("${book.percentage}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("${book.pagesRead} / ${book.totalPages} pages", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    if (book.status == ReadingStatus.READING) {
                        Text("${book.remainingPages} remaining", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (book.status == ReadingStatus.READING) {
                        QuickActionChip("-10", onClick = { onAdjustPages(-10) })
                        QuickActionChip("+10", onClick = { onAdjustPages(10) })
                    }
                    
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (book.isFavorite) SolarYellow.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f), CircleShape)
                    ) {
                        Icon(
                            if (book.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (book.isFavorite) SolarYellow else Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .background(CyberPurple.copy(alpha = 0.1f), CircleShape)
                            .border(1.dp, CyberPurple.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CyberPurple, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ReadingAnalyticsCard(completedCount: Int, totalReadPages: Int, totalBooks: Int, yearlyGoal: Int) {
    val progress = if (yearlyGoal > 0) (completedCount.toFloat() / yearlyGoal).coerceIn(0f, 1f) else 0f
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Library Stats", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = SolarYellow, modifier = Modifier.size(20.dp))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                StatColumn(totalBooks.toString(), "Total Books", ElectricBlue)
                StatColumn(completedCount.toString(), "Completed", Color(0xFF34C759))
                StatColumn(totalReadPages.toString(), "Pages Read", CyberPurple)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Yearly Goal: $completedCount/$yearlyGoal", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                Text("${(progress * 100).toInt()}%", color = Color(0xFF34C759), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun StatColumn(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 28.sp, fontWeight = FontWeight.Black)
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
    }
}

@Composable
fun ReadingGoalProgressCard(readingGoal: ReadingGoal, booksReadThisYear: Int, onEdit: () -> Unit) {
    val progress = if (readingGoal.yearlyGoal > 0) (booksReadThisYear.toFloat() / readingGoal.yearlyGoal).coerceIn(0f, 1f) else 0f
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF34C759), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reading Goal", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${(progress * 100).toInt()}%", color = Color(0xFF34C759), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Goal", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color = Color(0xFF34C759),
                trackColor = Color.White.copy(alpha = 0.05f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$booksReadThisYear books read", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                Text("Goal: ${readingGoal.yearlyGoal} books/year", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(readingGoal.monthlyGoal.toString(), color = ElectricBlue, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("Monthly Goal", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(readingGoal.pagesPerDay.toString(), color = SolarYellow, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("Pages/Day", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsStat(label: String, value: String, color: Color) {
    Column {
        Text(value, color = color, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text(label, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
    }
}

@Composable
fun ReadingEmptyState(tab: Int) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.AutoMirrored.Filled.LibraryBooks, contentDescription = null, tint = Color.White.copy(alpha = 0.1f), modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            if (tab == 0) "No active reads" else "Nothing to show here",
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 14.sp
        )
    }
}

@Composable
fun EditBookDialog(book: Book, onDismiss: () -> Unit, onUpdate: (Book) -> Unit, onDelete: () -> Unit) {
    var title by remember { mutableStateOf(book.title) }
    var author by remember { mutableStateOf(book.author) }
    var totalPages by remember { mutableStateOf(book.totalPages.toString()) }
    var price by remember { mutableStateOf(book.price.toString()) }
    var genre by remember { mutableStateOf(book.genre) }
    var language by remember { mutableStateOf(book.language) }
    var priority by remember { mutableStateOf(book.priority) }
    var status by remember { mutableStateOf(book.status) }
    var notes by remember { mutableStateOf(book.notes) }
    var coverUri by remember { mutableStateOf(book.coverUri) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            coverUri = ImageUtils.saveImageToInternalStorage(context, it)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = DeepVoid,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = CyberPurple, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Edit Book", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }

                Box(
                    modifier = Modifier
                        .size(100.dp, 130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable { launcher.launch("image/*") }
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    if (coverUri != null) {
                        AsyncImage(model = coverUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(32.dp))
                            Text("Add Cover", color = Color.White.copy(alpha = 0.2f), fontSize = 10.sp)
                        }
                    }
                }

                DialogTextField("Book Title", title, onValueChange = { title = it })
                DialogTextField("Author", author, onValueChange = { author = it })
                DialogTextField("Total Pages", totalPages, onValueChange = { totalPages = it }, icon = Icons.AutoMirrored.Filled.MenuBook, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                DialogTextField("Price (₹)", price, onValueChange = { price = it }, icon = Icons.Default.Payments, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                
                DialogTextField("Genre", genre, onValueChange = { genre = it }, icon = Icons.AutoMirrored.Filled.LibraryBooks)
                DropdownField("Language", language, listOf("English", "Hindi", "Marathi", "Other"), Icons.Default.Language) { language = it }
                DropdownField("Priority", priority.displayName, BookPriority.entries.map { it.displayName }, Icons.Default.Flag) { name ->
                    priority = BookPriority.entries.find { it.displayName == name } ?: BookPriority.MEDIUM
                }
                DropdownField("Status", status.displayName, ReadingStatus.entries.map { it.displayName }, Icons.Default.Bookmark) { name ->
                    status = ReadingStatus.entries.find { it.displayName == name } ?: ReadingStatus.WANT
                }

                DialogTextField("Notes", notes, onValueChange = { notes = it }, singleLine = false)

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    onUpdate(book.copy(
                                        title = title,
                                        author = author,
                                        totalPages = totalPages.toIntOrNull() ?: book.totalPages,
                                        price = price.toDoubleOrNull() ?: book.price,
                                        genre = genre,
                                        priority = priority,
                                        status = status,
                                        language = language,
                                        notes = notes,
                                        coverUri = coverUri,
                                        lastUpdated = Clock.System.now().toString()
                                    ))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Save", color = CyberPurple, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TagChip(tag: String, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .background(ElectricBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .border(1.dp, ElectricBlue.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(tag, color = ElectricBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = ElectricBlue,
                modifier = Modifier.size(10.dp).clickable { onRemove() }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PriorityChip(priority: BookPriority, isSelected: Boolean, onClick: () -> Unit) {
    val color = Color(priority.colorHex)
    Box(
        modifier = Modifier
            .height(36.dp)
            .background(if (isSelected) color.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
            .border(if (isSelected) 1.dp else 0.dp, color, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            priority.displayName.split(" ").last(),
            color = if (isSelected) color else Color.White.copy(alpha = 0.4f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AddBookDialog(onDismiss: () -> Unit, onAdd: (Book) -> Unit) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var totalPages by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("English") }
    var priority by remember { mutableStateOf(BookPriority.MEDIUM) }
    var status by remember { mutableStateOf(ReadingStatus.WANT) }
    var notes by remember { mutableStateOf("") }
    var coverUri by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            coverUri = ImageUtils.saveImageToInternalStorage(context, it)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = DeepVoid,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoStories, contentDescription = null, tint = CyberPurple, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Add Book", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }

                Box(
                    modifier = Modifier
                        .size(100.dp, 130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable { launcher.launch("image/*") }
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    if (coverUri != null) {
                        AsyncImage(model = coverUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(32.dp))
                            Text("Add Cover", color = Color.White.copy(alpha = 0.2f), fontSize = 10.sp)
                        }
                    }
                }

                DialogTextField("Book Title", title, onValueChange = { title = it })
                DialogTextField("Author", author, onValueChange = { author = it })
                DialogTextField("Total Pages", totalPages, onValueChange = { totalPages = it }, icon = Icons.AutoMirrored.Filled.MenuBook, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                DialogTextField("Price (₹)", price, onValueChange = { price = it }, icon = Icons.Default.Payments, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                
                DialogTextField("Genre", genre, onValueChange = { genre = it }, icon = Icons.AutoMirrored.Filled.LibraryBooks)
                DropdownField("Language", language, listOf("English", "Hindi", "Marathi", "Other"), Icons.Default.Language) { language = it }
                DropdownField("Priority", priority.displayName, BookPriority.entries.map { it.displayName }, Icons.Default.Flag) { name ->
                    priority = BookPriority.entries.find { it.displayName == name } ?: BookPriority.MEDIUM
                }
                DropdownField("Status", status.displayName, ReadingStatus.entries.map { it.displayName }, Icons.Default.Bookmark) { name ->
                    status = ReadingStatus.entries.find { it.displayName == name } ?: ReadingStatus.WANT
                }

                DialogTextField("Notes", notes, onValueChange = { notes = it }, singleLine = false)

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onAdd(Book(
                                    title = title,
                                    author = author,
                                    totalPages = totalPages.toIntOrNull() ?: 0,
                                    price = price.toDoubleOrNull() ?: 0.0,
                                    genre = genre,
                                    priority = priority,
                                    status = status,
                                    language = language,
                                    notes = notes,
                                    coverUri = coverUri
                                ))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Add Book", color = CyberPurple, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DialogTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = icon?.let { imageVector -> { Icon(imageVector, contentDescription = null, tint = CyberPurple.copy(alpha = 0.7f), modifier = Modifier.size(20.dp)) } },
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyberPurple,
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedLabelColor = CyberPurple,
            unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(label: String, selectedValue: String, options: List<String>, icon: ImageVector, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = CyberPurple.copy(alpha = 0.7f), modifier = Modifier.size(20.dp)) },
            trailingIcon = { 
                val emoji = when(label) {
                    "Genre" -> "📚"
                    "Priority" -> "🚩"
                    "Status" -> "🔖"
                    else -> ""
                }
                Text(emoji, modifier = Modifier.padding(end = 8.dp))
            },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberPurple,
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedLabelColor = CyberPurple,
                unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = DeepVoid
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = Color.White) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun EditReadingGoalDialog(currentGoal: ReadingGoal, onDismiss: () -> Unit, onSave: (ReadingGoal) -> Unit) {
    var yearlyGoal by remember { mutableStateOf(currentGoal.yearlyGoal.toString()) }
    var monthlyGoal by remember { mutableStateOf(currentGoal.monthlyGoal.toString()) }
    var pagesPerDay by remember { mutableStateOf(currentGoal.pagesPerDay.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepVoid,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Reading Targets 🎯", color = Color.White, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DialogTextField("Yearly Goal (Books)", yearlyGoal, onValueChange = { yearlyGoal = it }, icon = Icons.Default.EmojiEvents)
                DialogTextField("Monthly Goal (Books)", monthlyGoal, onValueChange = { monthlyGoal = it }, icon = Icons.Default.CalendarMonth)
                DialogTextField("Daily Goal (Pages)", pagesPerDay, onValueChange = { pagesPerDay = it }, icon = Icons.Default.AutoStories)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(currentGoal.copy(
                        yearlyGoal = yearlyGoal.toIntOrNull() ?: currentGoal.yearlyGoal,
                        monthlyGoal = monthlyGoal.toIntOrNull() ?: currentGoal.monthlyGoal,
                        pagesPerDay = pagesPerDay.toIntOrNull() ?: currentGoal.pagesPerDay
                    ))
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Update Goals")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.5f))
            }
        }
    )
}