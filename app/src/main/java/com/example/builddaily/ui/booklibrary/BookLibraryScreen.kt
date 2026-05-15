package com.example.builddaily.ui.booklibrary

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
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.builddaily.data.model.Book
import com.example.builddaily.data.model.BookGenre
import com.example.builddaily.data.model.BookPriority
import com.example.builddaily.data.model.BookStatus
import com.example.builddaily.data.repository.BookRepository
import com.example.builddaily.data.repository.ReadingGoal
import com.example.builddaily.ui.theme.CyberPurple
import com.example.builddaily.ui.theme.DeepVoid
import com.example.builddaily.ui.theme.ElectricBlue
import com.example.builddaily.ui.theme.MintGreen
import com.example.builddaily.ui.theme.SolarYellow
import com.example.builddaily.util.AnimatedCurrency
import com.example.builddaily.util.AnimatedNumber
import com.example.builddaily.util.AnimatedPercentage
import com.example.builddaily.util.CurrencyUtils

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
    val wantToRead by viewModel.wantToRead.collectAsState()
    val completed by viewModel.completed.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val archived by viewModel.archived.collectAsState()
    val displayedBooks by viewModel.displayedBooks.collectAsState()

    val totalPagesRead by viewModel.totalPagesRead.collectAsState(0)
    val completedCount by viewModel.completedCount.collectAsState(0)
    val totalBooks by viewModel.totalBooks.collectAsState(0)

    var showAddBookDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var selectedBook by remember { mutableStateOf<Book?>(null) }

    val motivationalQuotes = listOf(
        "Reading is dreaming with open eyes 📚",
        "Every page unlocks new wisdom ✨",
        "Your mind is your greatest asset 🧠",
        "Knowledge compounds over time 📖",
        "Small steps, big transformations 🌟"
    )
    var currentQuote by remember { mutableStateOf(motivationalQuotes.random()) }

    LaunchedEffect(books.size) {
        if (books.isNotEmpty()) {
            currentQuote = motivationalQuotes.random()
        }
    }

    Scaffold(
        containerColor = DeepVoid,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = CyberPurple, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Reading Vault", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showGoalDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Set Goals", tint = CyberPurple)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddBookDialog = true },
                containerColor = CyberPurple
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add book", tint = Color.White)
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
                PremiumReadingStatsCard(
                    totalBooks = totalBooks,
                    completedCount = completedCount,
                    totalPagesRead = totalPagesRead,
                    readingGoal = readingGoal
                )
            }

            item {
                PremiumReadingGoalCard(readingGoal = readingGoal, completedThisYear = completed.count { it.status == BookStatus.COMPLETED })
            }

            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = CyberPurple,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = CyberPurple
                        )
                    }
                ) {
                    Tab(selected = selectedTab == 0, onClick = { viewModel.setTab(0) }, text = { Text("Reading (${currentlyReading.size})", color = if (selectedTab == 0) CyberPurple else Color.White.copy(alpha = 0.6f)) })
                    Tab(selected = selectedTab == 1, onClick = { viewModel.setTab(1) }, text = { Text("Want (${wantToRead.size})", color = if (selectedTab == 1) ElectricBlue else Color.White.copy(alpha = 0.6f)) })
                    Tab(selected = selectedTab == 2, onClick = { viewModel.setTab(2) }, text = { Text("Done (${completed.size})", color = if (selectedTab == 2) MintGreen else Color.White.copy(alpha = 0.6f)) })
                    Tab(selected = selectedTab == 3, onClick = { viewModel.setTab(3) }, text = { Text("Fav (${favorites.size})", color = if (selectedTab == 3) SolarYellow else Color.White.copy(alpha = 0.6f)) })
                }
            }

            val displayedBooks = when (selectedTab) {
                0 -> currentlyReading
                1 -> wantToRead
                2 -> completed
                3 -> favorites
                else -> currentlyReading
            }

            if (displayedBooks.isEmpty()) {
                item {
                    PremiumBookEmptyState(tab = selectedTab, quote = currentQuote)
                }
            } else {
                items(displayedBooks, key = { it.id }) { book ->
                    PremiumBookCard(
                        book = book,
                        readingGoal = readingGoal,
                        onClick = { selectedBook = book },
                        onToggleFavorite = { viewModel.updateBook(book.copy(isFavorite = !book.isFavorite)) },
                        onDelete = { viewModel.deleteBook(book.id) },
                        onUpdateProgress = { pages -> viewModel.updateProgress(book, pages) }
                    )
                }
            }

            if (archived.isNotEmpty() && selectedTab != 3) {
                item {
                    Text("Archived (${archived.size})", color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                }
                items(archived.take(3), key = { "archived_${it.id}" }) { book ->
                    CompactArchivedBookCard(book = book, onRestore = { viewModel.updateBook(book.copy(status = BookStatus.WANT_TO_READ)) })
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }

    if (showAddBookDialog) {
        PremiumAddBookDialog(
            onDismiss = { showAddBookDialog = false },
            onAdd = { book ->
                viewModel.addBook(book)
                showAddBookDialog = false
            }
        )
    }

    if (showGoalDialog) {
        PremiumReadingGoalDialog(
            currentGoal = readingGoal,
            onDismiss = { showGoalDialog = false },
            onSave = { goal ->
                viewModel.updateReadingGoal(goal)
                showGoalDialog = false
            }
        )
    }

    selectedBook?.let { book ->
        PremiumBookDetailSheet(
            book = book,
            onDismiss = { selectedBook = null },
            onUpdate = { updatedBook ->
                viewModel.updateBook(updatedBook)
                selectedBook = updatedBook
            },
            onDelete = { viewModel.deleteBook(book.id); selectedBook = null }
        )
    }
}

@Composable
fun PremiumReadingStatsCard(
    totalBooks: Int,
    completedCount: Int,
    totalPagesRead: Int,
    readingGoal: ReadingGoal
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
                            CyberPurple.copy(alpha = 0.15f),
                            ElectricBlue.copy(alpha = 0.1f)
                        )
                    )
                )
                .border(1.dp, CyberPurple.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Library Stats", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = SolarYellow, modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBox(label = "Total Books", value = totalBooks.toString(), color = ElectricBlue)
                    StatBox(label = "Completed", value = completedCount.toString(), color = MintGreen)
                    StatBox(label = "Pages Read", value = CurrencyUtils.formatIndianNumber(totalPagesRead), color = CyberPurple)
                }

                if (readingGoal.yearlyGoal > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    val yearlyProgress = completedCount.toFloat() / readingGoal.yearlyGoal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Yearly Goal: ${completedCount}/${readingGoal.yearlyGoal}", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        AnimatedPercentage(targetValue = yearlyProgress.coerceIn(0f, 1f), style = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MintGreen))
                    }
                }
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedNumber(
            targetValue = value.toIntOrNull() ?: 0,
            style = androidx.compose.ui.text.TextStyle(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = color)
        )
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
    }
}

@Composable
fun PremiumReadingGoalCard(readingGoal: ReadingGoal, completedThisYear: Int) {
    val progress = if (readingGoal.yearlyGoal > 0) completedThisYear.toFloat() / readingGoal.yearlyGoal else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), animationSpec = tween(1000), label = "goal")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.horizontalGradient(listOf(MintGreen.copy(alpha = 0.1f), CyberPurple.copy(alpha = 0.05f))))
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
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MintGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reading Goal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    if (readingGoal.yearlyGoal > 0) {
                        AnimatedPercentage(targetValue = progress.coerceIn(0f, 1f), style = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MintGreen))
                    }
                }

                if (readingGoal.yearlyGoal > 0) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(brush = Brush.horizontalGradient(listOf(MintGreen, CyberPurple)))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$completedThisYear books read", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        Text("Goal: ${readingGoal.yearlyGoal} books/year", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Set your yearly reading goal to track progress!", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                }

                if (readingGoal.monthlyGoal > 0 || readingGoal.pagesPerDay > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (readingGoal.monthlyGoal > 0) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${readingGoal.monthlyGoal}", color = ElectricBlue, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                Text("Monthly Goal", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                            }
                        }
                        if (readingGoal.pagesPerDay > 0) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${readingGoal.pagesPerDay}", color = SolarYellow, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                Text("Pages/Day", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumBookCard(
    book: Book,
    readingGoal: ReadingGoal,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onUpdateProgress: (Int) -> Unit
) {
    val priorityColor = Color(book.priority.colorHex)
    val progress = if (book.pages > 0) book.pagesRead.toFloat() / book.pages else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), animationSpec = tween(800), label = "progress")

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
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(priorityColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(book.genre.emoji, fontSize = 28.sp)
                            if (book.pages > 0) {
                                Text("${(progress * 100).toInt()}%", color = priorityColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                book.title.ifEmpty { "Untitled Book" },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (book.isFavorite) {
                                Icon(Icons.Default.Favorite, contentDescription = "Favorite", tint = SolarYellow, modifier = Modifier.size(18.dp))
                            }
                        }

                        if (book.author.isNotEmpty()) {
                            Text("by ${book.author}", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, maxLines = 1)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            BookPriorityBadge(priority = book.priority, color = priorityColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(book.genre.displayName, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        }

                        if (book.pages > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animatedProgress)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(priorityColor)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${book.pagesRead}/${book.pages} pages", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
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
                        if (book.price > 0) {
                            AnimatedCurrency(
                                targetValue = book.price,
                                style = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ElectricBlue)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        if (book.status == BookStatus.CURRENTLY_READING && book.pages > 0) {
                            val pagesRemaining = book.pages - book.pagesRead
                            val daysLeft = if (readingGoal.pagesPerDay > 0) pagesRemaining / readingGoal.pagesPerDay else 0
                            if (daysLeft > 0) {
                                Text("$daysLeft days left", color = SolarYellow, fontSize = 12.sp, modifier = Modifier.background(SolarYellow.copy(alpha = 0.1f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }

                    Row {
                        if (book.status == BookStatus.CURRENTLY_READING && book.pages > 0) {
                            IconButton(onClick = { onUpdateProgress((book.pagesRead + 10).coerceAtMost(book.pages)) }, modifier = Modifier.size(32.dp).background(MintGreen.copy(alpha = 0.15f), CircleShape)) {
                                Icon(Icons.Default.Add, contentDescription = "Add Progress", tint = MintGreen, modifier = Modifier.size(16.dp))
                            }
                        }
                        IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                            Icon(if (book.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = "Toggle Favorite", tint = if (book.isFavorite) SolarYellow else Color.White.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookPriorityBadge(priority: BookPriority, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(priority.displayName, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PremiumBookEmptyState(tab: Int, quote: String) {
    val (title, subtitle, emoji) = when (tab) {
        0 -> Triple("No books currently reading", "Start your reading journey today!", "📖")
        1 -> Triple("Your reading wishlist is empty", "Add books you want to read", "📚")
        2 -> Triple("No completed books yet", "Finish your first book to see it here", "🎉")
        3 -> Triple("No favorite books yet", "Mark books as favorites to see them here", "❤️")
        else -> Triple("Your library awaits", "Add your first book", "✨")
    }

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
                        colors = listOf(CyberPurple.copy(alpha = 0.3f), Color.Transparent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(CyberPurple.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 35.sp)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text("\"$quote\"", color = CyberPurple.copy(alpha = 0.8f), fontSize = 13.sp, textAlign = TextAlign.Center, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
    }
}

@Composable
fun CompactArchivedBookCard(book: Book, onRestore: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.02f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(book.genre.emoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(book.title.ifEmpty { "Book" }, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Medium, maxLines = 1)
                    Text("Archived", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                }
                TextButton(onClick = onRestore) {
                    Text("Restore", color = ElectricBlue, fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumBookDetailSheet(
    book: Book,
    onDismiss: () -> Unit,
    onUpdate: (Book) -> Unit,
    onDelete: () -> Unit
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
                    Text(book.genre.emoji, fontSize = 40.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(book.title.ifEmpty { "Book" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        if (book.author.isNotEmpty()) Text("by ${book.author}", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
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
                DetailStatBox(label = "Pages", value = book.pages.toString(), color = ElectricBlue)
                DetailStatBox(label = "Read", value = book.pagesRead.toString(), color = MintGreen)
                DetailStatBox(label = "Genre", value = book.genre.displayName, color = CyberPurple)
            }

            if (book.pages > 0) {
                Spacer(modifier = Modifier.height(20.dp))

                val progress = book.pagesRead.toFloat() / book.pages
                val animatedProgress by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), animationSpec = tween(800), label = "detail")

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
                            .background(brush = Brush.horizontalGradient(listOf(MintGreen, CyberPurple)))
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "${(progress * 100).toInt()}% complete • ${book.pages - book.pagesRead} pages remaining",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            if (book.rating > 0) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Your Rating:", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    repeat(book.rating) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = SolarYellow, modifier = Modifier.size(20.dp))
                    }
                }
            }

            if (book.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text("Notes", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(book.notes, color = Color.White, fontSize = 14.sp)
            }

            if (book.favoriteQuotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Favorite Quotes", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("\"${book.favoriteQuotes}\"", color = CyberPurple, fontSize = 14.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }

            if (book.price > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Price: ", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                    AnimatedCurrency(targetValue = book.price, style = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ElectricBlue))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val statusOptions = BookStatus.entries.toList()
                statusOptions.forEach { status ->
                    val isSelected = book.status == status
                    TextButton(
                        onClick = { onUpdate(book.copy(status = status)) },
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) when (status) {
                                    BookStatus.CURRENTLY_READING -> MintGreen
                                    BookStatus.WANT_TO_READ -> ElectricBlue
                                    BookStatus.COMPLETED -> CyberPurple
                                    BookStatus.ARCHIVED -> Color.Gray
                                }.copy(alpha = 0.15f)
                                else Color.White.copy(alpha = 0.05f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            when (status) {
                                BookStatus.CURRENTLY_READING -> "Reading"
                                BookStatus.WANT_TO_READ -> "Want"
                                BookStatus.COMPLETED -> "Done"
                                BookStatus.ARCHIVED -> "Archive"
                            },
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DetailStatBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
    }
}

@Composable
fun PremiumAddBookDialog(onDismiss: () -> Unit, onAdd: (Book) -> Unit) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var pages by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf(BookGenre.OTHER) }
    var priority by remember { mutableStateOf(BookPriority.MEDIUM) }
    var status by remember { mutableStateOf(BookStatus.WANT_TO_READ) }
    var notes by remember { mutableStateOf("") }
    var showGenreDropdown by remember { mutableStateOf(false) }
    var showPriorityDropdown by remember { mutableStateOf(false) }
    var showStatusDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepVoid,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MenuBook, contentDescription = null, tint = CyberPurple)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Book", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Book Title") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberPurple, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text("Author") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberPurple, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = pages,
                        onValueChange = { pages = it },
                        label = { Text("Total Pages") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberPurple, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Book, contentDescription = null, tint = CyberPurple) }
                    )
                }
                item {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price (₹)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberPurple, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.MenuBook, contentDescription = null, tint = ElectricBlue) }
                    )
                }
                item {
                    OutlinedTextField(
                        value = genre.displayName,
                        onValueChange = {},
                        label = { Text("Genre") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberPurple, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showGenreDropdown = true }) {
                                Text(genre.emoji, fontSize = 18.sp)
                            }
                        }
                    )
                }
                item {
                    OutlinedTextField(
                        value = priority.displayName,
                        onValueChange = {},
                        label = { Text("Priority") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberPurple, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showPriorityDropdown = true }) {
                                Icon(Icons.Default.Flag, contentDescription = null, tint = CyberPurple)
                            }
                        }
                    )
                }
                item {
                    OutlinedTextField(
                        value = status.displayName,
                        onValueChange = {},
                        label = { Text("Status") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberPurple, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showStatusDropdown = true }) {
                                Icon(Icons.Default.Bookmark, contentDescription = null, tint = CyberPurple)
                            }
                        }
                    )
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberPurple, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newBook = Book(
                        title = title,
                        author = author,
                        price = price.toDoubleOrNull() ?: 0.0,
                        pages = pages.toIntOrNull() ?: 0,
                        genre = genre,
                        priority = priority,
                        status = status,
                        notes = notes
                    )
                    onAdd(newBook)
                },
                enabled = title.isNotBlank()
            ) {
                Text("Add Book", color = CyberPurple, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.5f))
            }
        }
    )

    if (showGenreDropdown) {
        GenreSelectionDialog(
            genres = BookGenre.entries.toList(),
            selected = genre,
            onSelect = {
                genre = it
                showGenreDropdown = false
            },
            onDismiss = { showGenreDropdown = false }
        )
    }

    if (showPriorityDropdown) {
        BookPrioritySelectionDialog(
            priorities = BookPriority.entries.toList(),
            selected = priority,
            onSelect = {
                priority = it
                showPriorityDropdown = false
            },
            onDismiss = { showPriorityDropdown = false }
        )
    }

    if (showStatusDropdown) {
        BookStatusSelectionDialog(
            statuses = BookStatus.entries.toList(),
            selected = status,
            onSelect = {
                status = it
                showStatusDropdown = false
            },
            onDismiss = { showStatusDropdown = false }
        )
    }
}

@Composable
fun GenreSelectionDialog(
    genres: List<BookGenre>,
    selected: BookGenre,
    onSelect: (BookGenre) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepVoid,
        title = { Text("Select Genre", color = Color.White) },
        text = {
            LazyColumn {
                items(genres.size) { index ->
                    val genre = genres[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(genre) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(genre.emoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            genre.displayName,
                            color = if (genre == selected) CyberPurple else Color.White,
                            fontWeight = if (genre == selected) FontWeight.Bold else FontWeight.Normal
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
fun BookPrioritySelectionDialog(
    priorities: List<BookPriority>,
    selected: BookPriority,
    onSelect: (BookPriority) -> Unit,
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
                        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
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
fun BookStatusSelectionDialog(
    statuses: List<BookStatus>,
    selected: BookStatus,
    onSelect: (BookStatus) -> Unit,
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
                            color = if (status == selected) CyberPurple else Color.White,
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

@Composable
fun PremiumReadingGoalDialog(
    currentGoal: ReadingGoal,
    onDismiss: () -> Unit,
    onSave: (ReadingGoal) -> Unit
) {
    var yearlyGoal by remember { mutableStateOf(currentGoal.yearlyGoal.toString()) }
    var monthlyGoal by remember { mutableStateOf(currentGoal.monthlyGoal.toString()) }
    var pagesPerDay by remember { mutableStateOf(currentGoal.pagesPerDay.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepVoid,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = CyberPurple)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reading Goals", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = yearlyGoal,
                    onValueChange = { yearlyGoal = it },
                    label = { Text("Yearly Goal (books)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberPurple, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.LocalLibrary, contentDescription = null, tint = CyberPurple) }
                )
                OutlinedTextField(
                    value = monthlyGoal,
                    onValueChange = { monthlyGoal = it },
                    label = { Text("Monthly Goal (books)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintGreen, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.AutoStories, contentDescription = null, tint = MintGreen) }
                )
                OutlinedTextField(
                    value = pagesPerDay,
                    onValueChange = { pagesPerDay = it },
                    label = { Text("Pages per Day") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SolarYellow, unfocusedBorderColor = Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = SolarYellow) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        ReadingGoal(
                            yearlyGoal = yearlyGoal.toIntOrNull() ?: 0,
                            monthlyGoal = monthlyGoal.toIntOrNull() ?: 0,
                            pagesPerDay = pagesPerDay.toIntOrNull() ?: 0
                        )
                    )
                }
            ) {
                Text("Save Goals", color = CyberPurple, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.5f))
            }
        }
    )
}