package com.example.builddaily.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class BottomNavItem(val label: String, val icon: ImageVector, val route: String) {
    Home("Home", Icons.Default.Home, "home"),
    History("History", Icons.Default.History, "history"),
    Stats("Stats", Icons.Default.BarChart, "stats"),
    More("More", Icons.Default.MoreHoriz, "more")
}

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onItemSelected: (BottomNavItem) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Main Container
        Row(
            modifier = Modifier
                .height(64.dp)
                .background(Color(0xFF121212), RoundedCornerShape(32.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem.entries.forEach { item ->
                val isSelected = currentRoute == item.route
                
                val itemBgColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF1E1E1E) else Color.Transparent,
                    label = "bg"
                )

                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(itemBgColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onItemSelected(item) }
                        )
                        .padding(horizontal = if (isSelected) 14.dp else 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Icon Circle
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1E1E1E),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) Color.Black else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        // Label Visibility
                        AnimatedVisibility(visible = isSelected) {
                            Text(
                                text = item.label,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
