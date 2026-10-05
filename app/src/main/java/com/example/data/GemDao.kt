package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GemDao {
    // Hidden Gems
    @Query("SELECT * FROM hidden_gems ORDER BY createdAt DESC")
    fun getAllGemsFlow(): Flow<List<HiddenGem>>

    @Query("SELECT * FROM hidden_gems ORDER BY createdAt DESC")
    suspend fun getAllGems(): List<HiddenGem>

    @Query("SELECT * FROM hidden_gems WHERE id = :gemId LIMIT 1")
    suspend fun getGemById(gemId: Int): HiddenGem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGem(gem: HiddenGem): Long

    @Query("DELETE FROM hidden_gems")
    suspend fun deleteAllGems()

    @Update
    suspend fun updateGem(gem: HiddenGem)

    // Users
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    // Activities
    @Query("SELECT * FROM gem_activities WHERE gemId = :gemId AND isActive = 1 ORDER BY updatedAt DESC")
    fun getActivitiesForGemFlow(gemId: Int): Flow<List<GemActivity>>

    @Query("SELECT * FROM gem_activities WHERE gemId = :gemId ORDER BY updatedAt DESC")
    suspend fun getActivitiesForGem(gemId: Int): List<GemActivity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: GemActivity): Long

    @Update
    suspend fun updateActivity(activity: GemActivity)

    @Query("DELETE FROM gem_activities WHERE id = :activityId")
    suspend fun deleteActivity(activityId: Int)

    // Reviews
    @Query("SELECT * FROM gem_reviews WHERE gemId = :gemId ORDER BY createdAt DESC")
    fun getReviewsForGemFlow(gemId: Int): Flow<List<GemReview>>

    @Query("SELECT * FROM gem_reviews WHERE gemId = :gemId ORDER BY createdAt DESC")
    suspend fun getReviewsForGem(gemId: Int): List<GemReview>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: GemReview): Long

    @Update
    suspend fun updateReview(review: GemReview)
}
