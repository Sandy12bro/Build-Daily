package com.example.builddaily.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BuyItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val price: Double = 0.0,
    val amountSaved: Double = 0.0,
    val priority: BuyPriority = BuyPriority.IMPORTANT,
    val notes: String = "",
    val link: String = "",
    val isPurchased: Boolean = false,
    val purchasedDate: String? = null,
    val createdAt: String = kotlinx.datetime.Clock.System.now().toString(),
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedDate: String? = null,
    val finalPurchasePrice: Double? = null
)

@Serializable
enum class BuyPriority(val displayName: String, val level: Int, val colorHex: Long) {
    MUST_BUY("🔥 Must Buy", 3, 0xFFFF3B30),
    IMPORTANT("⭐ Important", 2, 0xFFFF9500),
    MAYBE_LATER("💡 Maybe Later", 1, 0xFF8E8E93)
}

@Serializable
enum class ItemStatus(val displayName: String) {
    PLANNED("Planned"),
    SAVING_FOR("Saving For"),
    CAN_AFFORD("Can Afford"),
    PURCHASED("Purchased"),
    POSTPONED("Postponed")
}