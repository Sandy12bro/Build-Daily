package com.example.builddaily.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.builddaily.util.ActionMessage
import com.example.builddaily.util.ActionMessageManager
import com.example.builddaily.util.ActionType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun FlashMessageOverlay() {
    var currentMessage by remember { mutableStateOf<ActionMessage?>(null) }

    LaunchedEffect(Unit) {
        ActionMessageManager.messages.collectLatest { message ->
            currentMessage = message
            delay(3000)
            currentMessage = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 90.dp, start = 32.dp, end = 32.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = currentMessage != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            currentMessage?.let { msg ->
                val (color, icon) = when (msg.type) {
                    ActionType.ADDED -> com.example.builddaily.ui.theme.ElectricBlue to Icons.Default.AddCircle
                    ActionType.UPDATED -> com.example.builddaily.ui.theme.CyberPurple to Icons.Default.Edit
                    ActionType.DELETED -> Color(0xFFFF5252) to Icons.Default.Delete
                    ActionType.REPEATED -> Color(0xFFFF4081) to Icons.Default.Repeat
                    ActionType.COMPLETED -> com.example.builddaily.ui.theme.MintGreen to Icons.Default.CheckCircle
                    ActionType.INCOMPLETE -> Color.Gray to Icons.Default.RadioButtonUnchecked
                }

                Surface(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Black.copy(alpha = 0.9f),
                    tonalElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = msg.message,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
