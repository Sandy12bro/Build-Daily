package com.example.builddaily.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BuyItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val price: Double = 0.0,
    val imageUri: String? = null,
    val category: BuyCategory = BuyCategory.OTHER,
    val priority: BuyPriority = BuyPriority.IMPORTANT,
    val notes: String = "",
    val deadline: String? = null,
    val link: String = "",
    val isPurchased: Boolean = false,
    val purchasedDate: String? = null,
    val createdAt: String = kotlinx.datetime.Clock.System.now().toString(),
    val amountSaved: Double = 0.0,
    val itemStatus: ItemStatus = ItemStatus.PLANNED
)

@Serializable
enum class BuyCategory(val displayName: String, val emoji: String) {
    GADGETS("Gadgets", "📱"),
    BIKE_CAR("Bike/Car", "🏍️"),
    STUDY("Study", "📚"),
    GAMING("Gaming", "🎮"),
    FITNESS("Fitness", "💪"),
    FASHION("Fashion", "👕"),
    HOSTEL_ROOM("Hostel/Room", "🏠"),
    BOOKS("Books", "📖"),
    TECH_SETUP("Tech Setup", "💻"),
    INVESTMENTS("Investments", "📈"),
    FOOD("Food & Drinks", "🍕"),
    TRANSPORT("Transport", "🚌"),
    ENTERTAINMENT("Entertainment", "🎬"),
    OTHER("Other", "📦")
}

@Serializable
enum class BuyPriority(val displayName: String, val level: Int, val colorHex: Long) {
    CRITICAL("Critical", 5, 0xFFFF3B30),
    IMPORTANT("Important", 4, 0xFFFF9500),
    OPTIONAL("Optional", 3, 0xFF34C759),
    DREAM_ITEM("Dream Item", 2, 0xFFAF52DE),
    SOMEDAY("Someday", 1, 0xFF8E8E93)
}

@Serializable
enum class ItemStatus(val displayName: String) {
    PLANNED("Planned"),
    SAVING_FOR("Saving For"),
    CAN_AFFORD("Can Afford"),
    PURCHASED("Purchased"),
    POSTPONED("Postponed")
}