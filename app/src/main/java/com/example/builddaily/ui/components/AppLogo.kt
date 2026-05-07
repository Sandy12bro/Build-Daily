package com.example.builddaily.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.builddaily.R


@Composable
fun AppLogo(size: Dp = 40.dp) {
    Image(
        painter = painterResource(id = R.drawable.app_logo_final),
        contentDescription = "App Logo",
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
    )
}

@Composable
fun AppTitleWithLogo(
    title: String,
    showLogo: Boolean = true
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (showLogo) {
            AppLogo(size = 32.dp)
            Box(modifier = Modifier.padding(start = 12.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
