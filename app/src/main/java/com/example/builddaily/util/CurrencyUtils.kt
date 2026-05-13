package com.example.builddaily.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.builddaily.ui.theme.CyberPurple
import com.example.builddaily.ui.theme.ElectricBlue
import com.example.builddaily.ui.theme.MintGreen
import com.example.builddaily.ui.theme.SolarYellow
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

object CurrencyUtils {
    fun formatIndianRupees(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
        return "₹${formatter.format(amount.roundToLong())}"
    }

    fun formatIndianRupeesCompact(amount: Double): String {
        return when {
            amount >= 1_00_00_000 -> "₹${(amount / 1_00_00_000).roundToLong()}Cr"
            amount >= 1_00_000 -> "₹${(amount / 1_00_000).roundToLong()}L"
            amount >= 1_000 -> "₹${(amount / 1_000).roundToLong()}K"
            else -> formatIndianRupees(amount)
        }
    }

    fun formatIndianNumber(number: Int): String {
        val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
        return formatter.format(number)
    }

    fun formatPercentage(value: Float): String {
        return "${(value * 100).roundToLong()}%"
    }

    fun getPriorityColor(priorityLevel: Int): Color {
        return when (priorityLevel) {
            5 -> Color(0xFFFF3B30)
            4 -> Color(0xFFFF9500)
            3 -> Color(0xFF34C759)
            2 -> Color(0xFFAF52DE)
            else -> Color(0xFF8E8E93)
        }
    }

    fun getBudgetStatusColor(canAfford: Boolean, hasEnough: Boolean): Color {
        return when {
            canAfford -> MintGreen
            hasEnough -> SolarYellow
            else -> Color(0xFFFF3B30)
        }
    }

    fun getBudgetStatusText(canAfford: Boolean, hasEnough: Boolean): String {
        return when {
            canAfford -> "✅ Can Afford"
            hasEnough -> "⚠️ Save More"
            else -> "❌ Over Budget"
        }
    }

    fun calculateDailySaving(targetAmount: Double, currentSaved: Double, daysRemaining: Int): Double {
        val remaining = targetAmount - currentSaved
        return if (daysRemaining > 0 && remaining > 0) remaining / daysRemaining else 0.0
    }

    fun calculateDaysToAfford(targetAmount: Double, monthlySavings: Double): Int {
        return if (monthlySavings > 0) {
            val remaining = targetAmount
            (remaining / monthlySavings * 30).toInt()
        } else 0
    }
}

@Composable
fun AnimatedCurrency(
    targetValue: Double,
    style: TextStyle = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
    ),
    prefix: String = "₹",
    duration: Int = 1000
) {
    var animatedValue by remember { mutableStateOf(0f) }
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatable.animateTo(
            targetValue = targetValue.toFloat(),
            animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
        ) {
            animatedValue = value
        }
    }

    Text(
        text = "$prefix${CurrencyUtils.formatIndianRupees(animatedValue.toDouble()).replace("₹", "")}",
        style = style
    )
}

@Composable
fun AnimatedNumber(
    targetValue: Int,
    style: TextStyle = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
    ),
    duration: Int = 1000
) {
    var animatedValue by remember { mutableStateOf(0f) }
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatable.animateTo(
            targetValue = targetValue.toFloat(),
            animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
        ) {
            animatedValue = value
        }
    }

    Text(
        text = CurrencyUtils.formatIndianNumber(animatedValue.toInt()),
        style = style
    )
}

@Composable
fun AnimatedPercentage(
    targetValue: Float,
    style: TextStyle = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.White
    ),
    duration: Int = 800
) {
    var animatedValue by remember { mutableStateOf(0f) }
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatable.animateTo(
            targetValue = targetValue,
            animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
        ) {
            animatedValue = value
        }
    }

    Text(
        text = "${(animatedValue * 100).roundToLong()}%",
        style = style
    )
}