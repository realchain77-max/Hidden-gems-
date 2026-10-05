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
                val hasKenyaData = existingGems.any { it.title.contains("Pickled") || it.title.contains("Watamu") || it.title.contains("Nairobi") }
                if (hasKenyaData) {
                    Log.d("GemRepository", "Database already seeded with Kenya data. Total gems: ${existingGems.size}")
                    return
                } else {
                    // Clear legacy SF data to load Kenya dataset
                    gemDao.deleteAllGems()
                }
            }

            Log.d("GemRepository", "Seeding database with default Kenya Hidden Gems, Users, Activities, and Reviews...")

            // 1. Seed Users
            val userExplorerId = gemDao.insertUser(User(id = 1, username = "NairobiExplorer", email = "explorer@kenya.com", role = "explorer"))
            val userBusinessId1 = gemDao.insertUser(User(id = 2, username = "PickledKeManager", email = "hello@pickled.co.ke", role = "business"))
            val userBusinessId2 = gemDao.insertUser(User(id = 3, username = "CrabShackDabasoHost", email = "info@dabaso.co.ke", role = "business"))
            val userBusinessId3 = gemDao.insertUser(User(id = 4, username = "JahaziSwahiliCoffee", email = "coffee@jahazi.co.ke", role = "business"))

            // 2. Seed Kenya Hidden Gems
            // @pickledke - Artisanal Deli & Cafe
            val gem1Id = gemDao.insertGem(HiddenGem(
                id = 1,
                title = "Pickled (@pickledke)",
                description = "Boutique artisanal deli & leafy garden bistro in Lavington/Kilimani (@pickledke). Celebrated for handcrafted ferments, house-made quick pickles, sourdough gourmet sandwiches, smash burgers, and specialty iced cold brews in a relaxed open-air courtyard.",
                latitude = -1.2884,
                longitude = 36.7820,
                uploaderId = 2,
                isVerified = true,
                upvotes = 245,
                downvotes = 3,
                category = "Dining",
                captureTimestamp = System.currentTimeMillis() - 7200000L,
                captureLat = -1.2884,
                captureLng = 36.7820,
                gpsAccuracyMeters = 3.2f,
                isLiveVerified = true
            )).toInt()

            // Nairobi Arboretum
            val gem2Id = gemDao.insertGem(HiddenGem(
                id = 2,
                title = "Nairobi Arboretum Forest Glade",
                description = "30-hectare peaceful woodland sanctuary right in Kilimani/State House area. Features towering indigenous shade trees, winding gravel paths, playful Sykes monkeys, and hidden grassy glades ideal for quiet reading and weekend picnics.",
                latitude = -1.2750,
                longitude = 36.8080,
                uploaderId = 1,
                isVerified = true,
                upvotes = 182,
                downvotes = 4,
                category = "Parks",
                captureTimestamp = System.currentTimeMillis() - 14400000L,
                captureLat = -1.2750,
                captureLng = 36.8080,
                gpsAccuracyMeters = 4.1f,
                isLiveVerified = true
            )).toInt()

            // Koinange Secret Rooftop & Dawa Bar
            val gem3Id = gemDao.insertGem(HiddenGem(
                id = 3,
                title = "Koinange Secret Rooftop & Dawa Lounge",
                description = "Hidden rooftop hideaway perched high above central Nairobi CBD. Serves authentic Kenyan Dawa cocktails with vodka, lime, and local honey, fresh nyama choma grills, and live acoustic Afro-jazz background sets.",
                latitude = -1.2833,
                longitude = 36.8167,
                uploaderId = 1,
                isVerified = false,
                upvotes = 138,
                downvotes = 5,
                category = "Nightlife"
            )).toInt()

            // Watamu Crab Shack Dabaso
            val gem4Id = gemDao.insertGem(HiddenGem(
                id = 4,
                title = "Crab Shack Dabaso (Mida Creek)",
                description = "Raised wooden boardwalk suspended above the tranquil mangrove swamp of Mida Creek in Watamu. Famous for fresh mud crab samosas, grilled tiger prawns, coconut fish, and breathtaking sunset dhow cruises.",
                latitude = -3.3421,
                longitude = 39.9615,
                uploaderId = 3,
                isVerified = true,
                upvotes = 290,
                downvotes = 2,
                category = "Dining",
                captureTimestamp = System.currentTimeMillis() - 21600000L,
                captureLat = -3.3421,
                captureLng = 39.9615,
                gpsAccuracyMeters = 2.8f,
                isLiveVerified = true
            )).toInt()

            // Watamu Ocean Breeze Cove & Turtle Watch
            val gem5Id = gemDao.insertGem(HiddenGem(
                id = 5,
                title = "Watamu Ocean Breeze Cove & Turtle Refuge",
                description = "Secluded turquoise marine lagoon flanked by coral cliffs and powdery white sand. Watch sea turtle hatchlings safely make their journey to the ocean with local Watamu marine conservationists.",
                latitude = -3.3550,
                longitude = 39.9800,
                uploaderId = 1,
                isVerified = false,
                upvotes = 210,
                downvotes = 1,
                category = "Beaches"
            )).toInt()

            // Jahazi Coffee House - Mombasa Old Town
            val gem6Id = gemDao.insertGem(HiddenGem(
                id = 6,
                title = "Jahazi Coffee House (Mombasa Old Town)",
                description = "Authentic Swahili heritage coffee house nestled in ancient Mombasa Old Town. Sit on traditional hand-carved floor cushions, sip spiced cardamom Kahwa coffee, and enjoy warm mahamri with coconut tea.",
                latitude = -4.0590,
                longitude = 39.6780,
                uploaderId = 4,
                isVerified = true,
                upvotes = 195,
                downvotes = 2,
                category = "Historic"
            )).toInt()

            // Fort Jesus Harbor Night Courtyard - Mombasa
            val gem7Id = gemDao.insertGem(HiddenGem(
                id = 7,
                title = "Fort Jesus Harbor Night Courtyard",
                description = "16th-century Portuguese fortress built on coral rock guarding Mombasa Harbor. Illuminated by torches at night for traditional Swahili banquets, live Taarab music, and sound-and-light history shows.",
                latitude = -4.0628,
                longitude = 39.6795,
                uploaderId = 1,
                isVerified = true,
                upvotes = 220,
                downvotes = 4,
                category = "Historic"
            )).toInt()

            // KiteSurfing Lagoon & Forty Thieves - Diani
            val gem8Id = gemDao.insertGem(HiddenGem(
                id = 8,
                title = "KiteSurfing Lagoon & Diani Oceanfront",
                description = "Iconic tropical paradise on Diani Beach with pristine turquoise tidal waters, ideal for kitesurfing, oceanfront seafood grills, fresh coconut water, and sunset palm tree swings.",
                latitude = -4.2790,
                longitude = 39.5920,
                uploaderId = 1,
                isVerified = false,
                upvotes = 310,
                downvotes = 6,
                category = "Beaches"
            )).toInt()

            // Gedi Ruins Sunken Forest - Malindi
            val gem9Id = gemDao.insertGem(HiddenGem(
                id = 9,
                title = "Gedi Ruins Sunken Forest (Malindi)",
                description = "Mystical 12th-century ruined Swahili stone city hidden deep inside a lush indigenous forest dominated by giant baobabs, Sykes monkeys, and rare wildlife.",
                latitude = -3.3080,
                longitude = 40.0160,
                uploaderId = 1,
                isVerified = false,
                upvotes = 165,
                downvotes = 3,
                category = "Historic"
            )).toInt()

            // Hell's Gate Gorge & Fischer's Tower - Naivasha
            val gem10Id = gemDao.insertGem(HiddenGem(
                id = 10,
                title = "Hell's Gate Gorge & Fischer's Tower",
                description = "Dramatic towering volcanic rock pillars, red sandstone gorges, and natural geothermal hot springs in Naivasha where you can ride bicycles alongside zebras and gazelles.",
                latitude = -0.8870,
                longitude = 36.3190,
                uploaderId = 1,
                isVerified = false,
                upvotes = 240,
                downvotes = 5,
                category = "Scenic"
            )).toInt()

            // The Alchemist & Yard Market - Westlands
            val gem11Id = gemDao.insertGem(HiddenGem(
                id = 11,
                title = "The Alchemist Creative Yard",
                description = "Vibrant creative collective featuring food trucks, local artisan pop-up boutiques, open-air cinema nights, and DJ music stages surrounded by lush potted plants in Westlands, Nairobi.",
                latitude = -1.2645,
                longitude = 36.8045,
                uploaderId = 1,
                isVerified = true,
                upvotes = 280,
                downvotes = 7,
                category = "Nightlife"
            )).toInt()

            // Shela Beach Floating Dhow Bar - Lamu
            val gem12Id = gemDao.insertGem(HiddenGem(
                id = 12,
                title = "Shela Beach Floating Dhow Bar (Lamu)",
                description = "Floating wooden dhow anchored off Shela Beach in Lamu. Catch gentle sea breezes, enjoy fresh mango passion juice or cocktails, and watch traditional dhow sails drift by at twilight.",
                latitude = -2.2686,
                longitude = 40.9020,
                uploaderId = 1,
                isVerified = false,
                upvotes = 190,
                downvotes = 2,
                category = "Scenic"
            )).toInt()

            // 3. Seed Activities (For Verified Business Spots)
            // @pickledke Activities
            gemDao.insertActivity(GemActivity(
                gemId = gem1Id,
                activityName = "Artisanal Ferment Tasting & Brunch Flight",
                description = "Sample our signature house-fermented kimchi, quick pickles, sourdough toasties, and cold brewed Kenyan coffees in the open garden. Includes a jar of artisan pickled chili garlic to take home!",
                schedule = "Saturdays & Sundays 10:30 AM - 3:00 PM",
                priceLevel = 2,
                isActive = true
            ))

            // Crab Shack Dabaso Activities
            gemDao.insertActivity(GemActivity(
                gemId = gem4Id,
                activityName = "Mida Creek Mangrove Sunset Cruise & Mud Crab Dinner",
                description = "A peaceful guided canoe ride through Mida Creek's mangrove forest as birds return to nest at dusk, followed by a fresh mud crab & seafood banquet at our boardwalk shack.",
                schedule = "Daily at 4:30 PM - 7:30 PM",
                priceLevel = 3,
                isActive = true
            ))

            // Jahazi Coffee House Activities
            gemDao.insertActivity(GemActivity(
                gemId = gem6Id,
                activityName = "Swahili Kahwa Coffee Ceremony & Old Town Storytelling",
                description = "Experience traditional ginger-spiced Swahili coffee brewing in brass samovars, served with fresh coconut mahamri while hearing historic tales of ancient Mombasa seafaring.",
                schedule = "Daily 3:00 PM - 6:00 PM",
                priceLevel = 1,
                isActive = true
            ))

            // 4. Seed Reviews & Replies
            // @pickledke Reviews
            gemDao.insertReview(GemReview(
                gemId = gem1Id,
                userId = 1,
                username = "NairobiFoodie_Amani",
                rating = 5,
                crowdDensity = 2,
                comment = "Hands down one of my favorite spots in Nairobi! The sourdough smash burger with pickled jalapenos at @pickledke is out of this world. The outdoor garden vibe is so refreshing.",
                createdAt = System.currentTimeMillis() - 86400000 * 2
            ))
            gemDao.insertReview(GemReview(
                gemId = gem1Id,
                userId = 1,
                username = "CoffeeAndTravels_KE",
                rating = 5,
                crowdDensity = 2,
                comment = "Amazing atmosphere, great iced lattes, and the ferments are incredible. Perfect spot for remote work or a weekend brunch with friends.",
                createdAt = System.currentTimeMillis() - 86400000,
                businessReply = "Asante sana! We are delighted you loved the smash burgers and ferments. See you again in the garden soon!"
            ))

            // Crab Shack Dabaso Reviews
            gemDao.insertReview(GemReview(
                gemId = gem4Id,
                userId = 1,
                username = "WatamuWanderer",
                rating = 5,
                crowdDensity = 1,
                comment = "Unforgettable experience! Walking on the elevated boardwalk through the mangroves into the sunset feels like entering another realm. The crab samosas are a 10/10.",
                createdAt = System.currentTimeMillis() - 86400000 * 4,
                businessReply = "Karibu Watamu! We are so happy you enjoyed the mangrove sunset and mud crab samosas!"
            ))

            // Nairobi Arboretum Reviews
            gemDao.insertReview(GemReview(
                gemId = gem2Id,
                userId = 1,
                username = "NatureWalker_Nbo",
                rating = 5,
                crowdDensity = 1,
                comment = "A serene green escape right near CBD. Watched monkeys jumping in the canopy while enjoying a quiet afternoon picnic.",
                createdAt = System.currentTimeMillis() - 86400000 * 7
            ))

            Log.d("GemRepository", "Seed completed successfully.")
        } catch (e: Exception) {
            Log.e("GemRepository", "Error seeding database: ${e.message}", e)
        }
    }
}
