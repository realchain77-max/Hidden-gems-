package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val email: String,
    val role: String = "explorer", // "explorer" or "business"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "hidden_gems")
data class HiddenGem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val uploaderId: Int? = null,
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val category: String = "Scenic",
    val captureTimestamp: Long? = null,
    val captureLat: Double? = null,
    val captureLng: Double? = null,
    val gpsAccuracyMeters: Float? = null,
    val isLiveVerified: Boolean = false,
    val photoBase64: String? = null,
    val cloudSynced: Boolean = false,
    val cloudId: String? = null,
    val aiLocationAnalysis: String? = null
)

@Entity(tableName = "gem_activities")
data class GemActivity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gemId: Int,
    val activityName: String,
    val description: String,
    val schedule: String, // e.g. "Fridays at 8 PM"
    val priceLevel: Int, // 1 to 4
    val isActive: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gem_reviews")
data class GemReview(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gemId: Int,
    val userId: Int,
    val username: String, // Cached for easy rendering
    val rating: Int, // 1 to 5
    val crowdDensity: Int, // 1=Empty, 2=Moderate, 3=Crowded
    val comment: String,
    val businessReply: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
