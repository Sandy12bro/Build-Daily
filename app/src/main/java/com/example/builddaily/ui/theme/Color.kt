package com.example.builddaily.ui.theme

import androidx.compose.ui.graphics.Color

// Premium Galactic Dark Palette (Base)
val SpaceBlack = Color(0xFF050505)
val DeepVoid = Color(0xFF0D0D12)
val NebulaGrey = Color(0xFF16161D)
val GlassLayer = Color(0xFF23232E)

// Multi-Color Palette (Vibrant & Diverse)
val CyberPurple = Color(0xFF8B5CF6)   // Core Action
val ElectricBlue = Color(0xFF3B82F6)    // Focus / Primary
val MintGreen = Color(0xFF10B981)      // Completed / Success
val SolarYellow = Color(0xFFF59E0B)    // Pending / Warning
val FlareRed = Color(0xFFEF4444)       // Error / Urgent
val OceanTeal = Color(0xFF14B8A6)      // Alternate
val BerryPink = Color(0xFFEC4899)      // Special

// Support Colors
val StarWhite = Color(0xFFF8FAFC)
val MutedSlate = Color(0xFF64748B)

// Color Palette for Random/Category Assignments
val TaskCategoryColors = listOf(
    CyberPurple,
    ElectricBlue,
    MintGreen,
    SolarYellow,
    OceanTeal,
    BerryPink
)

// Legacy mapping (pointing to new vibrant colors)
val OnyxBlack = SpaceBlack
val SurfaceGrey = DeepVoid
val GlassGrey = GlassLayer
val BlueprintLavender = ElectricBlue // Changed from Purple to Blue for freshness
val ConstructionEmerald = MintGreen
val ErrorRose = FlareRed
val Slate400 = MutedSlate

val Purple80 = CyberPurple
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = BerryPink

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)