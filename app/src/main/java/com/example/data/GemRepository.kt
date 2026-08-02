package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlin.math.*

class GemRepository(private val gemDao: GemDao) {

    val allGemsFlow: Flow<List<HiddenGem>> = gemDao.getAllGemsFlow()

    suspend fun getAllGems(): List<HiddenGem> = gemDao.getAllGems()

    suspend fun getGemById(gemId: Int): HiddenGem? = gemDao.getGemById(gemId)

    suspend fun insertGem(gem: HiddenGem): Long = gemDao.insertGem(gem)

    suspend fun updateGem(gem: HiddenGem) = gemDao.updateGem(gem)

    // Users
    suspend fun getUserById(userId: Int): User? = gemDao.getUserById(userId)
    
    suspend fun getUserByEmail(email: String): User? = gemDao.getUserByEmail(email)

    suspend fun insertUser(user: User): Long = gemDao.insertUser(user)

    // Activities
    fun getActivitiesForGemFlow(gemId: Int): Flow<List<GemActivity>> = gemDao.getActivitiesForGemFlow(gemId)

    suspend fun getActivitiesForGem(gemId: Int): List<GemActivity> = gemDao.getActivitiesForGem(gemId)

    suspend fun insertActivity(activity: GemActivity): Long = gemDao.insertActivity(activity)

    suspend fun updateActivity(activity: GemActivity) = gemDao.updateActivity(activity)

    suspend fun deleteActivity(activityId: Int) = gemDao.deleteActivity(activityId)

    // Reviews
    fun getReviewsForGemFlow(gemId: Int): Flow<List<GemReview>> = gemDao.getReviewsForGemFlow(gemId)

    suspend fun getReviewsForGem(gemId: Int): List<GemReview> = gemDao.getReviewsForGem(gemId)

    suspend fun insertReview(review: GemReview): Long = gemDao.insertReview(review)

    suspend fun updateReview(review: GemReview) = gemDao.updateReview(review)

    // Haversine Distance Search in Meters
    fun getGemsWithinRadius(
        centerLat: Double,
        centerLng: Double,
        radiusInMeters: Double,
        allGems: List<HiddenGem>
    ): List<HiddenGem> {
        return allGems.filter { gem ->
            calculateDistanceInMeters(centerLat, centerLng, gem.latitude, gem.longitude) <= radiusInMeters
        }
    }

    fun calculateDistanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth's radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    // Pre-populate data with realistic items
    suspend fun seedDatabaseIfEmpty() {
        try {
            val existingGems = gemDao.getAllGems()
            if (existingGems.isNotEmpty()) {
                Log.d("GemRepository", "Database already seeded. Total gems: ${existingGems.size}")
                return
            }

            Log.d("GemRepository", "Seeding database with default Hidden Gems and Users...")

            // 1. Seed Users
            val userExplorerId = gemDao.insertUser(User(id = 1, username = "ExplorerJess", email = "lopezjessie891@gmail.com", role = "explorer"))
            val userBusinessId1 = gemDao.insertUser(User(id = 2, username = "MacondrayFloraCafe", email = "flora@macondray.com", role = "business"))
            val userBusinessId2 = gemDao.insertUser(User(id = 3, username = "SewardDerbyManager", email = "manager@sewardslides.org", role = "business"))
            val userBusinessId3 = gemDao.insertUser(User(id = 4, username = "LandsEndLabyrinthCo", email = "walks@landsend.org", role = "business"))

            // 2. Seed Gems (Latitudes/Longitudes around SF area, bounding box: 37.74 to 37.82, -122.53 to -122.40)
            val gem1Id = gemDao.insertGem(HiddenGem(
                id = 1,
                title = "Sutro Baths Ruins",
                description = "Faded concrete ruins of a massive 19th-century public indoor swimming pool complex, sitting right on the edge of the Pacific Ocean. Mystical coastal breeze, saltwater pools, and a dark rock cave to walk through.",
                latitude = 37.7796,
                longitude = -122.5137,
                uploaderId = 1,
                isVerified = false,
                upvotes = 120,
                downvotes = 5,
                category = "Beaches"
            )).toInt()

            val gem2Id = gemDao.insertGem(HiddenGem(
                id = 2,
                title = "Seward Street Slides",
                description = "Two long, steep concrete slides tucked away inside a quiet neighborhood park in Castro/Noe Valley. Adults and children slide down on pieces of cardboard provided by locals. Sensation of retro neighborhood fun!",
                latitude = 37.7578,
                longitude = -122.4398,
                uploaderId = 3,
                isVerified = true,
                upvotes = 84,
                downvotes = 2,
                category = "Parks"
            )).toInt()

            val gem3Id = gemDao.insertGem(HiddenGem(
                id = 3,
                title = "The Wave Organ",
                description = "An acoustic wave-activated organ built on a jetty in the marina, featuring PVC and concrete pipes that gurgle, bubble, and sigh with the movement of changing bay tides. Best enjoyed at high tide!",
                latitude = 37.8085,
                longitude = -122.4367,
                uploaderId = 1,
                isVerified = false,
                upvotes = 62,
                downvotes = 8,
                category = "Scenic"
            )).toInt()

            val gem4Id = gemDao.insertGem(HiddenGem(
                id = 4,
                title = "Macondray Lane",
                description = "A hidden, lush pedestrian-only lane with rustic wooden paths, tiny historic cottages, climbing roses, and towering green canopies in Russian Hill. Inspiration for the famous fictional 'Barbary Lane'.",
                latitude = 37.8002,
                longitude = -122.4172,
                uploaderId = 2,
                isVerified = true,
                upvotes = 45,
                downvotes = 1,
                category = "Historic"
            )).toInt()

            val gem5Id = gemDao.insertGem(HiddenGem(
                id = 5,
                title = "Point Bonita Lighthouse",
                description = "A stunning lighthouse clinging to a rocky cliff in the Marin Headlands, accessed via a high suspension bridge over roaring ocean waters. Breathtaking views of the Golden Gate Strait and crashing waves.",
                latitude = 37.8155,
                longitude = -122.5295,
                uploaderId = 1,
                isVerified = false,
                upvotes = 110,
                downvotes = 4,
                category = "Scenic"
            )).toInt()

            val gem6Id = gemDao.insertGem(HiddenGem(
                id = 6,
                title = "Lands End Labyrinth",
                description = "A peaceful rock labyrinth created by a local artist on a scenic plateau above Mile Rock Beach. Provides a beautiful space for mindfulness walk and contemplation with the Golden Gate Bridge in the background.",
                latitude = 37.7881,
                longitude = -122.5058,
                uploaderId = 4,
                isVerified = true,
                upvotes = 95,
                downvotes = 3,
                category = "Scenic"
            )).toInt()

            // 3. Seed Activities (For Verified Business Spots)
            // Seward Street Slides Activities
            gemDao.insertActivity(GemActivity(
                gemId = gem2Id,
                activityName = "Seward Slide Derby & Cards",
                description = "Join the weekly Slide Derby! We supply heavy-duty waxed cardboard and friction-reducing chalk for the ultimate speed sliding experience. Includes a local slide racer badge!",
                schedule = "Fridays and Saturdays at 4:00 PM - 6:00 PM",
                priceLevel = 1,
                isActive = true
            ))

            // Macondray Lane Activities
            gemDao.insertActivity(GemActivity(
                gemId = gem4Id,
                activityName = "Flora, Poetry & Matcha Walk",
                description = "A guided historical walking tour highlighting rare botanical varieties, old Russian Hill literature, culminating in an elegant ceremonial matcha tea service on our hidden courtyard patio.",
                schedule = "Sundays at 10:00 AM - 12:00 PM",
                priceLevel = 2,
                isActive = true
            ))

            // Lands End Labyrinth Activities
            gemDao.insertActivity(GemActivity(
                gemId = gem6Id,
                activityName = "Sunset Labyrinth Meditation & Solstice Chants",
                description = "A serene evening group meditation. We walk the concentric stone pathways in silent mindfulness, accompanied by ambient handpan music, then watch the sun dip below the Pacific Horizon.",
                schedule = "Thursdays at 7:30 PM",
                priceLevel = 3,
                isActive = true
            ))

            // 4. Seed Reviews & Replies
            // Sutro Baths
            gemDao.insertReview(GemReview(
                gemId = gem1Id,
                userId = 1,
                username = "UrbanHikingJack",
                rating = 5,
                crowdDensity = 2,
                comment = "This is my absolute favorite place in San Francisco. Standing on the crumbling concrete walls with waves crashing right next to you is thrilling and beautiful. Must check out the cave!",
                createdAt = System.currentTimeMillis() - 86400000 * 3
            ))
            gemDao.insertReview(GemReview(
                gemId = gem1Id,
                userId = 2,
                username = "SF_Local_Guide",
                rating = 4,
                crowdDensity = 3,
                comment = "Lovely spot, but it gets incredibly crowded on warm weekends. The old tunnels can get muddy so wear decent shoes. Great history!",
                createdAt = System.currentTimeMillis() - 86400000
            ))

            // Seward Street Slides
            gemDao.insertReview(GemReview(
                gemId = gem2Id,
                userId = 1,
                username = "AdrenalineMom",
                rating = 5,
                crowdDensity = 1,
                comment = "Amazing community spot! The slides are faster than they look. Grab some cardboard from the bin. Wear jeans so your legs don't get scratched up by the concrete side rails.",
                createdAt = System.currentTimeMillis() - 86400000 * 5,
                businessReply = "We are so glad you and your family enjoyed it! We try to make sure the slide entry is cleared and fresh cardboard is in the recycle container daily. Slide fast!"
            ))

            // The Wave Organ
            gemDao.insertReview(GemReview(
                gemId = gem3Id,
                userId = 1,
                username = "AcousticEnthusiast",
                rating = 3,
                crowdDensity = 1,
                comment = "Concept is awesome, but you must look up the tide schedule before coming. At low tide, it's virtually silent. Come during high tide or incoming tides for those deep gurgling organ chords!",
                createdAt = System.currentTimeMillis() - 86400000 * 10
            ))

            Log.d("GemRepository", "Seed completed successfully.")
        } catch (e: Exception) {
            Log.e("GemRepository", "Error seeding database: ${e.message}", e)
        }
    }
}
