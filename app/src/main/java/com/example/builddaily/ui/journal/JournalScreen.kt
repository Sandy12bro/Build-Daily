package com.example.builddaily.ui.journal

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.builddaily.data.repository.UserStatsRepository
import com.example.builddaily.ui.theme.*
import com.example.builddaily.util.ActionMessageManager
import com.example.builddaily.util.ActionType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    onBack: () -> Unit,
    statsRepository: UserStatsRepository
) {
    val context = LocalContext.current
    val repository = remember { JournalRepository(context) }
    val viewModel = remember { JournalViewModel(repository) }

    val journals by viewModel.filteredJournals.collectAsState()
    val stickyNotes by viewModel.stickyNotes.collectAsState()
    val monthlyCovers by viewModel.monthlyCovers.collectAsState()
    val stats by viewModel.stats.collectAsState()
    
    val isPasscodeConfigured by viewModel.isPasscodeConfigured.collectAsState()
    val isSessionUnlocked by viewModel.isSessionUnlocked.collectAsState()

    // UI state for creating/editing journals
    var newTitle by remember { mutableStateOf("") }
    var newContent by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("focused") }
    var moodIntensity by remember { mutableStateOf(3f) }
    var selectedTextColor by remember { mutableStateOf("#FFFFFF") }
    var activeFolder by remember { mutableStateOf("Personal") }
    
    // UI state for creating sticky notes
    var showAddStickyDialog by remember { mutableStateOf(false) }
    var stickyTitle by remember { mutableStateOf("") }
    var stickyContent by remember { mutableStateOf("") }
    var stickyColorTheme by remember { mutableStateOf("Purple") }
    var stickyType by remember { mutableStateOf("quick ideas") }

    // Setup / Auth states
    var showLockerSetupDialog by remember { mutableStateOf(false) }
    var inputPasscodeBuffer by remember { mutableStateOf("") }
    var lockerErrorAnim by remember { mutableStateOf(false) }

    // Cover custom overrides
    var showEditCoverDialog by remember { mutableStateOf(false) }
    val currentMonthStr = remember {
        SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date())
    }
    val currentCover = remember(monthlyCovers, currentMonthStr) {
        viewModel.getOrCreateCoverConfigForMonth(currentMonthStr)
    }

    // AI prompt cycling index
    var promptIndex by remember { mutableStateOf(0) }

    // Breathing wellness animation helper
    val infiniteTransition = rememberInfiniteTransition()
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    // --- SECURE DIARY LOCK SCREEN INTERACTION ---
    if (isPasscodeConfigured && !isSessionUnlocked) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SpaceBlack)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = CyberPurple, modifier = Modifier.size(56.dp))
                Text(
                    "DIARY VAULT LOCKED",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
                Text(
                    "Enter your secure 4-digit Passcode to decrypt records.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                // Keypad Entry dots
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    for (i in 0 until 4) {
                        val isFilled = i < inputPasscodeBuffer.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) ElectricBlue else Color.White.copy(alpha = 0.1f))
                                .border(1.dp, if (isFilled) Color.Transparent else Color.White.copy(alpha = 0.2f), CircleShape)
                        )
                    }
                }

                if (lockerErrorAnim) {
                    Text("Incorrect Passcode. Try again.", color = FlareRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Glowing Keypad Matrix
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "⌫")
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    keys.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { char ->
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(DeepVoid)
                                        .border(1.dp, CyberPurple.copy(alpha = 0.3f), CircleShape)
                                        .clickable {
                                            lockerErrorAnim = false
                                            when (char) {
                                                "C" -> inputPasscodeBuffer = ""
                                                "⌫" -> if (inputPasscodeBuffer.isNotEmpty()) inputPasscodeBuffer = inputPasscodeBuffer.dropLast(1)
                                                else -> if (inputPasscodeBuffer.length < 4) {
                                                    inputPasscodeBuffer += char
                                                    if (inputPasscodeBuffer.length == 4) {
                                                        val correct = viewModel.verifyPasscode(inputPasscodeBuffer)
                                                        if (!correct) {
                                                            lockerErrorAnim = true
                                                            inputPasscodeBuffer = ""
                                                            ActionMessageManager.postMessage("Access Denied", ActionType.DELETED)
                                                        } else {
                                                            ActionMessageManager.postMessage("Vault Unlocked ✨", ActionType.COMPLETED)
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        char,
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = { viewModel.disablePasscode() }) {
                    Text("Emergency Override (Clear Lock)", color = MutedSlate, fontSize = 10.sp)
                }
            }
        }
        return // Short circuit rendering if locked
    }

    // --- MAIN JOURNAL & NOTES SCREEN ---
    Scaffold(
        containerColor = SpaceBlack,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Personal Diary", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberPurple.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("VAULT ACTIVE", color = CyberPurple, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showLockerSetupDialog = true }) {
                        Icon(if (isPasscodeConfigured) Icons.Default.Lock else Icons.Default.LockOpen, contentDescription = "Lock Settings", tint = if (isPasscodeConfigured) CyberPurple else Color.White.copy(alpha = 0.5f))
                    }
                    if (isPasscodeConfigured) {
                        IconButton(onClick = { 
                            viewModel.lockSessionManually() 
                            ActionMessageManager.postMessage("Vault Securely Locked", ActionType.UPDATED)
                        }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Lock Session", tint = FlareRed.copy(alpha = 0.8f))
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. DIGITAL LIFE BOOK MONTHLY HEADER COVER
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(DeepVoid, CyberPurple.copy(alpha = 0.2f)),
                                start = androidx.compose.ui.geometry.Offset.Zero,
                                end = androidx.compose.ui.geometry.Offset.Infinite
                            )
                        )
                        .border(1.dp, CyberPurple.copy(alpha = 0.4f), RoundedCornerShape(28.dp))
                        .clickable { showEditCoverDialog = true }
                ) {
                    // Ambient blurred shapes
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 20.dp, y = (-20).dp)
                            .blur(40.dp)
                            .background(ElectricBlue.copy(alpha = 0.3f), CircleShape)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    currentMonthStr.uppercase(),
                                    color = CyberPurple,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    currentCover.customTitle,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(Icons.Default.Edit, contentDescription = "Edit Cover", tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                        }

                        Column {
                            Text(
                                "DOMINANT VIBE: ${currentCover.dominantMood.uppercase()}",
                                color = ElectricBlue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "✨ Memory Recap: ${currentCover.favoriteMomentSummary}",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            // 2. TODAY'S MOOD TRACKING PANEL
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "EMOTIONAL ARCHITECTURE",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    
                    val moodOptions = listOf(
                        "happy" to "😊",
                        "focused" to "🎯",
                        "calm" to "🧘",
                        "motivated" to "⚡",
                        "tired" to "🔋",
                        "anxious" to "🌀",
                        "stressed" to "⚠️"
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(moodOptions) { (moodName, emoji) ->
                            val sel = selectedMood == moodName
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (sel) CyberPurple.copy(alpha = 0.2f) else DeepVoid)
                                    .border(1.dp, if (sel) CyberPurple else Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                    .clickable { selectedMood = moodName }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(emoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        moodName.capitalize(),
                                        color = if (sel) Color.White else Color.White.copy(alpha = 0.5f),
                                        fontSize = 12.sp,
                                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // Intensity Selector
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Intensity:", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Slider(
                            value = moodIntensity,
                            onValueChange = { moodIntensity = it },
                            valueRange = 1f..5f,
                            steps = 3,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = CyberPurple,
                                activeTrackColor = CyberPurple.copy(alpha = 0.6f),
                                inactiveTrackColor = DeepVoid
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${moodIntensity.toInt()}/5", color = CyberPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 3. QUICK / RICH JOURNAL CREATION CONSOLE
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(DeepVoid)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .scale(breathingPulse)
                                        .background(MintGreen, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "MINDFUL WRITING FOCUS", 
                                    color = MintGreen, 
                                    fontSize = 10.sp, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // Smart AI Prompts switcher
                            TextButton(
                                onClick = {
                                    val promptList = viewModel.smartPrompts
                                    promptIndex = (promptIndex + 1) % promptList.size
                                    newContent = promptList[promptIndex] + "\n\n"
                                    ActionMessageManager.postMessage("Loaded Smart Prompt 🧠", ActionType.ADDED)
                                },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, null, tint = SolarYellow, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI Prompt", color = SolarYellow, fontSize = 10.sp)
                            }
                        }

                        // Title field
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            placeholder = { Text("Title your thought...", color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        )

                        Divider(color = Color.White.copy(alpha = 0.05f))

                        // Body text editor
                        OutlinedTextField(
                            value = newContent,
                            onValueChange = { newContent = it },
                            placeholder = { Text("Write your long-form memories, reflections, or notes here. Markdown syntax supported...", color = Color.White.copy(alpha = 0.2f), fontSize = 13.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(selectedTextColor)),
                                unfocusedTextColor = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(selectedTextColor))
                            )
                        )

                        // Formatting & Color bar controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Text Style trigger simulation
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { newContent += "**Bold** " }, modifier = Modifier.size(28.dp)) {
                                    Text("B", color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                IconButton(onClick = { newContent += "*Italic* " }, modifier = Modifier.size(28.dp)) {
                                    Text("I", color = Color.White.copy(alpha = 0.6f), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 12.sp)
                                }
                                IconButton(onClick = { newContent += "- [ ] " }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Checklist, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                }
                                IconButton(onClick = { newContent += "\n- " }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.FormatListBulleted, null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                }
                            }

                            // Text Color options swatches
                            val colorSwatches = listOf("#FFFFFF", "#8B5CF6", "#3B82F6", "#10B981", "#F59E0B", "#EC4899")
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                colorSwatches.forEach { hex ->
                                    val c = Color(android.graphics.Color.parseColor(hex))
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(c)
                                            .border(1.dp, if (selectedTextColor == hex) Color.White else Color.Transparent, CircleShape)
                                            .clickable { selectedTextColor = hex }
                                    )
                                }
                            }
                        }

                        // Attachments and Launch Button Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(onClick = { ActionMessageManager.postMessage("Media Vault Triggered", ActionType.ADDED) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Image, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                                }
                                IconButton(onClick = { ActionMessageManager.postMessage("Audio Mic Recording Ready", ActionType.ADDED) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Mic, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                                }
                                IconButton(onClick = { ActionMessageManager.postMessage("Drawing Surface Initialized", ActionType.ADDED) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Brush, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                                }
                            }

                            Button(
                                onClick = {
                                    if (newContent.isNotBlank() || newTitle.isNotBlank()) {
                                        val finalTitle = if (newTitle.isNotBlank()) newTitle else "Reflection log"
                                        viewModel.addJournal(
                                            title = finalTitle,
                                            content = newContent,
                                            mood = selectedMood,
                                            moodIntensity = moodIntensity,
                                            textColorHex = selectedTextColor,
                                            tags = listOf("Personal", selectedMood),
                                            folder = activeFolder
                                        )
                                        newTitle = ""
                                        newContent = ""
                                        ActionMessageManager.postMessage("Memory Saved Successfully ✨", ActionType.ADDED)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                                shape = RoundedCornerShape(16.dp),
                                enabled = newContent.isNotBlank() || newTitle.isNotBlank()
                            ) {
                                Text("Inscribe Memory", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 4. FLOATING STICKY NOTES SYSTEM
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "FLOATING THOUGHT CAPTURES",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        TextButton(
                            onClick = { showAddStickyDialog = true },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(20.dp)
                        ) {
                            Text("+ Create Sticky Note", color = ElectricBlue, fontSize = 12.sp)
                        }
                    }

                    if (stickyNotes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.02f))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No quick sticky ideas pinned. Launch one above!", color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp)
                        }
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(stickyNotes, key = { it.id }) { note ->
                                val noteColor = when (note.colorTheme) {
                                    "Purple" -> CyberPurple
                                    "Cyan" -> ElectricBlue
                                    "Yellow" -> SolarYellow
                                    "Pink" -> BerryPink
                                    else -> MintGreen
                                }
                                Box(
                                    modifier = Modifier
                                        .width(160.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(noteColor.copy(alpha = 0.15f))
                                        .border(1.dp, noteColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                        .padding(14.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                note.type.uppercase(),
                                                color = noteColor,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                            IconButton(
                                                onClick = { viewModel.deleteStickyNote(note.id) },
                                                modifier = Modifier.size(16.dp)
                                            ) {
                                                Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(10.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        if (note.title.isNotBlank()) {
                                            Text(note.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Spacer(modifier = Modifier.height(2.dp))
                                        }
                                        Text(
                                            note.content,
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 11.sp,
                                            maxLines = 4
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. CALENDAR JOURNAL VIEW SYSTEM MATRIX
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "CHRONOLOGICAL JOURNAL MATRIX",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(DeepVoid)
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            // Dummy Week view mapping days 1 to 7
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                                weekDays.forEach { d ->
                                    Text(d, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Generate matrix mapping calendar indicator colors
                            val writtenDatesMap = journals.groupBy { it.dateStr }
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val baseCal = Calendar.getInstance()
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (i in 0 until 7) {
                                    val cal = baseCal.clone() as Calendar
                                    cal.add(Calendar.DAY_OF_MONTH, i - 3) // Surround current day
                                    val dateKey = sdf.format(cal.time)
                                    val dayNum = cal.get(Calendar.DAY_OF_MONTH)
                                    val dayLogs = writtenDatesMap[dateKey]
                                    val hasLog = dayLogs != null && dayLogs.isNotEmpty()
                                    val isToday = i == 3

                                    val dotColor = if (hasLog) {
                                        when(dayLogs?.firstOrNull()?.mood) {
                                            "happy" -> SolarYellow
                                            "focused" -> ElectricBlue
                                            "stressed" -> FlareRed
                                            "motivated" -> MintGreen
                                            else -> CyberPurple
                                        }
                                    } else Color.Transparent

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(4.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isToday) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.02f))
                                            .border(1.dp, if (isToday) ElectricBlue else Color.Transparent, RoundedCornerShape(12.dp))
                                            .clickable { 
                                                ActionMessageManager.postMessage("Selected Date: $dateKey", ActionType.INCOMPLETE) 
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                "$dayNum",
                                                color = if (hasLog) Color.White else Color.White.copy(alpha = 0.3f),
                                                fontSize = 11.sp,
                                                fontWeight = if (hasLog) FontWeight.Bold else FontWeight.Normal
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(dotColor)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. ANALYTICS & INSIGHTS HUD
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DeepVoid)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text("Total Pages", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${stats.totalWritten}", color = ElectricBlue, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${stats.totalWords} Lifetime Words", color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DeepVoid)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text("Mindful Streak", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${stats.currentStreak} Days", color = MintGreen, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Consistency Arc Live", color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp)
                        }
                    }
                }
            }

            // 7. DAILY REFLECTION SECTION CARDS
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "REFLECTIVE MINDFULNESS PRESETS",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    val reflectionItems = listOf(
                        "Today's Wins" to "What went wonderfully well today?",
                        "Lessons Learned" to "What valuable truth did life reveal?",
                        "Gratitude Anchor" to "Name three small things you cherish.",
                        "Tomorrow's Goals" to "Define a core metric for tomorrow."
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(reflectionItems) { (title, sub) ->
                            Box(
                                modifier = Modifier
                                    .width(220.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DeepVoid)
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                    .clickable {
                                        newTitle = title
                                        newContent = "**$title**\n\n- "
                                    }
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Text(title, color = CyberPurple, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(sub, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 8. CHRONOLOGICAL JOURNAL TIMELINE & PREVIOUS ENTRIES FEED
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "INSCRIBED CHRONICLES FEED",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    if (journals.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No journal records established yet.", color = Color.White.copy(alpha = 0.2f), fontSize = 12.sp)
                        }
                    }
                }
            }

            items(journals, key = { it.id }) { entry ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(DeepVoid)
                        .border(
                            1.dp, 
                            if (entry.isPinned) CyberPurple else Color.White.copy(alpha = 0.05f), 
                            RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(entry.folder.uppercase(), color = ElectricBlue, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(entry.dateStr, color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.togglePinJournal(entry) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        if (entry.isPinned) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Pin",
                                        tint = if (entry.isPinned) SolarYellow else Color.White.copy(alpha = 0.3f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.deleteJournal(entry.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, null, tint = FlareRed.copy(alpha = 0.4f), modifier = Modifier.size(14.dp))
                                }
                            }
                        }

                        if (entry.title.isNotBlank()) {
                            Text(entry.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        // Preview content mapping customized user choices cleanly
                        Text(
                            entry.content,
                            color = Color(android.graphics.Color.parseColor(entry.textColorHex)).copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )

                        // Tags bar
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                entry.tags.forEach { t ->
                                    Text("#$t", color = CyberPurple.copy(alpha = 0.7f), fontSize = 10.sp)
                                }
                            }
                            Text("Mood: ${entry.mood.capitalize()} (${entry.moodIntensity.toInt()}/5)", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }

    // --- MODAL DIALOGS ---

    // A. Add Sticky Note Dialog
    if (showAddStickyDialog) {
        AlertDialog(
            onDismissRequest = { showAddStickyDialog = false },
            containerColor = DeepVoid,
            titleContentColor = Color.White,
            title = { Text("Launch Sticky Note", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = stickyTitle,
                        onValueChange = { stickyTitle = it },
                        label = { Text("Short Header", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = stickyContent,
                        onValueChange = { stickyContent = it },
                        label = { Text("Sticky Content", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Column {
                        Text("Note Arc Theme:", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Purple", "Cyan", "Yellow", "Pink").forEach { c ->
                                val sel = stickyColorTheme == c
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (sel) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                                        .clickable { stickyColorTheme = c }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(c, color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (stickyContent.isNotBlank() || stickyTitle.isNotBlank()) {
                            viewModel.addStickyNote(stickyTitle, stickyContent, stickyColorTheme, stickyType)
                            stickyTitle = ""
                            stickyContent = ""
                            showAddStickyDialog = false
                            ActionMessageManager.postMessage("Sticky Idea Added Pinned", ActionType.ADDED)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Text("Pin Note", color = SpaceBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStickyDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.5f))
                }
            }
        )
    }

    // B. Locker Passcode Config Dialog
    if (showLockerSetupDialog) {
        var setupPin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showLockerSetupDialog = false },
            containerColor = DeepVoid,
            titleContentColor = Color.White,
            title = { Text("Secure Diary Passcode", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Set up a secret 4-digit keypad combination to lock your persistent second brain records securely.", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    OutlinedTextField(
                        value = setupPin,
                        onValueChange = { if (it.length <= 4) setupPin = it },
                        label = { Text("4-Digit PIN", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (setupPin.length == 4) {
                            viewModel.setupPasscode(setupPin)
                            showLockerSetupDialog = false
                            ActionMessageManager.postMessage("Passcode Configured Successfully", ActionType.COMPLETED)
                        } else if (setupPin.isEmpty()) {
                            viewModel.disablePasscode()
                            showLockerSetupDialog = false
                            ActionMessageManager.postMessage("Vault Protection Disabled", ActionType.DELETED)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPurple)
                ) {
                    Text(if (setupPin.isEmpty()) "Disable Lock" else "Enable Security", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLockerSetupDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.5f))
                }
            }
        )
    }

    // C. Monthly Cover Edit Dialog
    if (showEditCoverDialog) {
        var editedTitle by remember { mutableStateOf(currentCover.customTitle) }
        var editedSummary by remember { mutableStateOf(currentCover.favoriteMomentSummary) }
        AlertDialog(
            onDismissRequest = { showEditCoverDialog = false },
            containerColor = DeepVoid,
            titleContentColor = Color.White,
            title = { Text("Customize Monthly Chapter", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        label = { Text("Chapter Title", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedSummary,
                        onValueChange = { editedSummary = it },
                        label = { Text("Favorite Moment Recap", color = Color.White.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveMonthlyCoverOverride(
                            currentCover.copy(customTitle = editedTitle, favoriteMomentSummary = editedSummary)
                        )
                        showEditCoverDialog = false
                        ActionMessageManager.postMessage("Chapter Header Customizations Applied", ActionType.UPDATED)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPurple)
                ) {
                    Text("Save Cover", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditCoverDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.5f))
                }
            }
        )
    }
}
