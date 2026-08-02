package com.example.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GemViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = GemRepository(database.gemDao())

    // Active Authentication Manager
    val authManager = AuthManager(application, database.gemDao())
    val currentUser: StateFlow<User?> = authManager.currentUser
    val authState: StateFlow<AuthState> = authManager.authState
    val isFirebaseAvailable: StateFlow<Boolean> = authManager.isFirebaseAvailable

    // Helper method to login as a guest
    fun loginAsGuest() {
        viewModelScope.launch {
            val guestUser = User(
                id = 9999,
                username = "GuestExplorer",
                email = "guest@gems.com",
                role = "explorer",
                createdAt = System.currentTimeMillis()
            )
            database.gemDao().insertUser(guestUser)
            // Inject directly into current user state through mock fallback
            authManager.loginWithEmail("explorer@gems.com", "any") // Seed a mock user
        }
    }

    fun signUpWithEmail(email: String, password: String, username: String, role: String) {
        viewModelScope.launch {
            authManager.signUpWithEmail(email, password, username, role)
        }
    }

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            authManager.loginWithEmail(email, password)
        }
    }

    fun signInWithGoogle(context: Context, fallbackRole: String) {
        viewModelScope.launch {
            authManager.signInWithGoogle(context, fallbackRole)
        }
    }

    fun signInWithPasskey(context: Context) {
        viewModelScope.launch {
            authManager.signInWithPasskey(context)
        }
    }

    fun registerPasskey(context: Context, username: String) {
        viewModelScope.launch {
            authManager.registerPasskey(context, username)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authManager.signOut()
        }
    }

    // Map Viewport / Bounding box center coordinates
    // SF City Center default
    private val _mapCenterLat = MutableStateFlow(37.7850)
    val mapCenterLat: StateFlow<Double> = _mapCenterLat.asStateFlow()

    private val _mapCenterLng = MutableStateFlow(-122.4600)
    val mapCenterLng: StateFlow<Double> = _mapCenterLng.asStateFlow()

    // Filters & Queries
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _radiusLimitInMeters = MutableStateFlow(15000.0) // 15km default (covers SF)
    val radiusLimitInMeters: StateFlow<Double> = _radiusLimitInMeters.asStateFlow()

    private val _verifiedFilter = MutableStateFlow<Boolean?>(null) // null = all, true = verified, false = unverified
    val verifiedFilter: StateFlow<Boolean?> = _verifiedFilter.asStateFlow()

    private val _minRatingFilter = MutableStateFlow(0) // 0 to 5
    val minRatingFilter: StateFlow<Int> = _minRatingFilter.asStateFlow()

    // 1. Airbnb Style horizontal categories
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // 2. Navigation App Modes: "Discovery", "Map", "LocationSensor"
    private val _appMode = MutableStateFlow("Discovery")
    val appMode: StateFlow<String> = _appMode.asStateFlow()

    // 3. User Favorites Setup
    private val _favoriteGemIds = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteGemIds: StateFlow<Set<Int>> = _favoriteGemIds.asStateFlow()

    // 4. Thumbs Up/Down Voting Track map (gemId -> "up" or "down" or null)
    private val _votedGems = MutableStateFlow<Map<Int, String>>(emptyMap())
    val votedGems: StateFlow<Map<Int, String>> = _votedGems.asStateFlow()

    // 5. Simulated/Sensor User Coordinates
    private val _userLocationLat = MutableStateFlow(37.7850)
    val userLocationLat: StateFlow<Double> = _userLocationLat.asStateFlow()

    private val _userLocationLng = MutableStateFlow(-122.4600)
    val userLocationLng: StateFlow<Double> = _userLocationLng.asStateFlow()

    private val _isSensorRunning = MutableStateFlow(false)
    val isSensorRunning: StateFlow<Boolean> = _isSensorRunning.asStateFlow()

    private val _showLandingPage = MutableStateFlow(true)
    val showLandingPage: StateFlow<Boolean> = _showLandingPage.asStateFlow()

    fun dismissLandingPage() {
        _showLandingPage.value = false
    }

    // Selected Gem Detail Sheet State
    private val _selectedGemId = MutableStateFlow<Int?>(null)
    val selectedGemId: StateFlow<Int?> = _selectedGemId.asStateFlow()

    private val _selectedGem = MutableStateFlow<HiddenGem?>(null)
    val selectedGem: StateFlow<HiddenGem?> = _selectedGem.asStateFlow()

    private val _selectedGemActivities = MutableStateFlow<List<GemActivity>>(emptyList())
    val selectedGemActivities: StateFlow<List<GemActivity>> = _selectedGemActivities.asStateFlow()

    private val _selectedGemReviews = MutableStateFlow<List<GemReview>>(emptyList())
    val selectedGemReviews: StateFlow<List<GemReview>> = _selectedGemReviews.asStateFlow()

    // "Drop a Pin" State
    private val _isDroppingPin = MutableStateFlow(false)
    val isDroppingPin: StateFlow<Boolean> = _isDroppingPin.asStateFlow()

    private val _droppedLat = MutableStateFlow(0.0)
    val droppedLat: StateFlow<Double> = _droppedLat.asStateFlow()

    private val _droppedLng = MutableStateFlow(0.0)
    val droppedLng: StateFlow<Double> = _droppedLng.asStateFlow()

    private val _droppedCity = MutableStateFlow("San Francisco")
    val droppedCity: StateFlow<String> = _droppedCity.asStateFlow()

    private val _isGeocoding = MutableStateFlow(false)
    val isGeocoding: StateFlow<Boolean> = _isGeocoding.asStateFlow()

    private val _aiDescription = MutableStateFlow("")
    val aiDescription: StateFlow<String> = _aiDescription.asStateFlow()

    private val _isGeneratingAI = MutableStateFlow(false)
    val isGeneratingAI: StateFlow<Boolean> = _isGeneratingAI.asStateFlow()

    // Core Filtered Gems List (recalculated reactively in Kotlin)
    private val _filteredGems = MutableStateFlow<List<HiddenGem>>(emptyList())
    val filteredGems: StateFlow<List<HiddenGem>> = _filteredGems.asStateFlow()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    init {
        viewModelScope.launch {
            // Seed default values on first run
            repository.seedDatabaseIfEmpty()
            
            // Initial load of gems
            updateFilteredGems()
            
            // Listen to flow changes from Room to update UI reactively
            repository.allGemsFlow.collect {
                updateFilteredGems()
            }
        }

        // Listen for selection changes to load nested items
        viewModelScope.launch {
            _selectedGemId.collect { gemId ->
                if (gemId != null) {
                    val gem = repository.getGemById(gemId)
                    _selectedGem.value = gem
                    if (gem != null) {
                        // Load activities
                        repository.getActivitiesForGemFlow(gemId).collect {
                            _selectedGemActivities.value = it
                        }
                    }
                } else {
                    _selectedGem.value = null
                    _selectedGemActivities.value = emptyList()
                    _selectedGemReviews.value = emptyList()
                }
            }
        }

        // Separate collection for reviews flow to avoid locking
        viewModelScope.launch {
            _selectedGemId.collect { gemId ->
                if (gemId != null) {
                    repository.getReviewsForGemFlow(gemId).collect {
                        _selectedGemReviews.value = it
                    }
                }
            }
        }
    }

    // Explicit recomputation method for spatial and text filters
    fun updateFilteredGems() {
        viewModelScope.launch {
            val gems = repository.getAllGems()
            val centerLat = _mapCenterLat.value
            val centerLng = _mapCenterLng.value
            val search = _searchQuery.value
            val radius = _radiusLimitInMeters.value
            val verified = _verifiedFilter.value
            val category = _selectedCategory.value

            // 1. Filter by radius (ST_DWithin equivalent)
            var result = repository.getGemsWithinRadius(centerLat, centerLng, radius, gems)

            // 2. Filter by search query
            if (search.isNotBlank()) {
                result = result.filter { gem ->
                    gem.title.contains(search, ignoreCase = true) ||
                            gem.description.contains(search, ignoreCase = true) ||
                            gem.category.contains(search, ignoreCase = true)
                }
            }

            // 3. Filter by verified status
            if (verified != null) {
                result = result.filter { it.isVerified == verified }
            }

            // 4. Airbnb Horizontal Category Filter
            if (category != "All") {
                result = result.filter { it.category.equals(category, ignoreCase = true) }
            }

            _filteredGems.value = result
        }
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
        updateFilteredGems()
    }

    fun setAppMode(mode: String) {
        _appMode.value = mode
        updateFilteredGems()
    }

    fun toggleFavorite(gemId: Int) {
        val currentFavs = _favoriteGemIds.value
        if (currentFavs.contains(gemId)) {
            _favoriteGemIds.value = currentFavs - gemId
        } else {
            _favoriteGemIds.value = currentFavs + gemId
        }
    }

    fun upvoteGem(gemId: Int) {
        viewModelScope.launch {
            val gem = repository.getGemById(gemId) ?: return@launch
            val votesMap = _votedGems.value
            val previousVote = votesMap[gemId]
            
            var newUpvotes = gem.upvotes
            var newDownvotes = gem.downvotes
            
            if (previousVote == "up") {
                newUpvotes = (newUpvotes - 1).coerceAtLeast(0)
                _votedGems.value = votesMap - gemId
            } else {
                newUpvotes += 1
                if (previousVote == "down") {
                    newDownvotes = (newDownvotes - 1).coerceAtLeast(0)
                }
                _votedGems.value = votesMap + (gemId to "up")
            }
            
            val updated = gem.copy(upvotes = newUpvotes, downvotes = newDownvotes)
            repository.updateGem(updated)
            
            if (_selectedGemId.value == gemId) {
                _selectedGem.value = updated
            }
        }
    }

    fun downvoteGem(gemId: Int) {
        viewModelScope.launch {
            val gem = repository.getGemById(gemId) ?: return@launch
            val votesMap = _votedGems.value
            val previousVote = votesMap[gemId]
            
            var newUpvotes = gem.upvotes
            var newDownvotes = gem.downvotes
            
            if (previousVote == "down") {
                newDownvotes = (newDownvotes - 1).coerceAtLeast(0)
                _votedGems.value = votesMap - gemId
            } else {
                newDownvotes += 1
                if (previousVote == "up") {
                    newUpvotes = (newUpvotes - 1).coerceAtLeast(0)
                }
                _votedGems.value = votesMap + (gemId to "down")
            }
            
            val updated = gem.copy(upvotes = newUpvotes, downvotes = newDownvotes)
            repository.updateGem(updated)
            
            if (_selectedGemId.value == gemId) {
                _selectedGem.value = updated
            }
        }
    }

    fun toggleLocationSensor(active: Boolean) {
        _isSensorRunning.value = active
    }

    fun setUserLocation(lat: Double, lng: Double) {
        _userLocationLat.value = lat
        _userLocationLng.value = lng
        updateFilteredGems()
    }

    // Toggle Role (for easy testing of the 2 complete workflows)
    fun toggleUserRole() {
        val user = currentUser.value ?: return
        val newRole = if (user.role == "explorer") "business" else "explorer"
        authManager.updateCurrentUserRole(newRole)
        Log.d("GemViewModel", "Switched active user to role: $newRole")
    }

    fun setMapCenter(lat: Double, lng: Double) {
        _mapCenterLat.value = lat
        _mapCenterLng.value = lng
        updateFilteredGems()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        updateFilteredGems()
    }

    fun updateRadiusLimit(radius: Double) {
        _radiusLimitInMeters.value = radius
        updateFilteredGems()
    }

    fun updateVerifiedFilter(verified: Boolean?) {
        _verifiedFilter.value = verified
        updateFilteredGems()
    }

    fun updateMinRatingFilter(minRating: Int) {
        _minRatingFilter.value = minRating
        updateFilteredGems()
    }

    fun selectGem(gemId: Int?) {
        _selectedGemId.value = gemId
    }

    // Workflow A: Long press pin drop reverse geocoding
    fun startPinDrop(latitude: Double, longitude: Double) {
        _droppedLat.value = latitude
        _droppedLng.value = longitude
        _isDroppingPin.value = true
        _aiDescription.value = ""

        // Reverse geocoding locally (robust SF sectors)
        viewModelScope.launch {
            _isGeocoding.value = true
            withContext(Dispatchers.Default) {
                _droppedCity.value = reverseGeocodeLocal(latitude, longitude)
            }
            _isGeocoding.value = false
            
            // Auto generate an AI description for this spot
            generateAIDescriptionForSpot(latitude, longitude)
        }
    }

    fun cancelPinDrop() {
        _isDroppingPin.value = false
    }

    private fun reverseGeocodeLocal(lat: Double, lng: Double): String {
        return when {
            lng < -122.49 -> "Lands End & Outer Richmond"
            lat > 37.798 && lng between (-122.435 to -122.41) -> "Russian Hill & North Beach"
            lat > 37.795 && lng between (-122.47 to -122.435) -> "Marina & Cow Hollow"
            lat between (37.75 to 37.77) && lng between (-122.45 to -122.42) -> "Castro & Noe Valley"
            lat between (37.765 to 37.785) && lng between (-122.51 to -122.465) -> "Golden Gate Park"
            lat between (37.75 to 37.77) && lng between (-122.51 to -122.47) -> "Outer Sunset"
            else -> "San Francisco Historic Core"
        }
    }

    // Helper infix for double range
    private infix fun Double.between(range: Pair<Double, Double>): Boolean {
        return this >= range.first && this <= range.second
    }

    // Call Gemini API to generate an interesting backstory/description for dropped pin
    private fun generateAIDescriptionForSpot(lat: Double, lng: Double) {
        val neighborhood = _droppedCity.value
        val defaultDescription = "A quiet, off-the-beaten-path sanctuary in $neighborhood, offering panoramic vistas and peaceful isolation. Ideal for contemplative walks and escaping the urban rush."
        
        viewModelScope.launch {
            _isGeneratingAI.value = true
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                _aiDescription.value = defaultDescription
                _isGeneratingAI.value = false
                return@launch
            }

            val prompt = "You are a local tour guide. Write a brief, captivating 2-sentence description of an off-the-beaten-path 'hidden gem' located at coordinates ($lat, $lng) in the $neighborhood neighborhood of San Francisco. Make it sound cozy and ready to be explored. Keep it under 250 characters."

            val result: String = withContext(Dispatchers.IO) {
                try {
                    val mediaType = "application/json; charset=utf-8".toMediaType()
                    
                    // Standard, type-safe json object creation with built-in Android JSONObject
                    val reqJson = JSONObject()
                    val contentsArray = JSONArray()
                    val firstContent = JSONObject()
                    val partsArray = JSONArray()
                    val textPart = JSONObject()
                    textPart.put("text", prompt)
                    partsArray.put(textPart)
                    firstContent.put("parts", partsArray)
                    contentsArray.put(firstContent)
                    reqJson.put("contents", contentsArray)

                    val req = reqJson.toString()
                    
                    val request = Request.Builder()
                        .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                        .post(req.toRequestBody(mediaType))
                        .build()

                    okHttpClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val bodyString = response.body?.string() ?: ""
                            val jsonResponse = JSONObject(bodyString)
                            val candidates = jsonResponse.getJSONArray("candidates")
                            val firstCandidate = candidates.getJSONObject(0)
                            val content = firstCandidate.getJSONObject("content")
                            val parts = content.getJSONArray("parts")
                            parts.getJSONObject(0).getString("text").trim()
                        } else {
                            Log.e("GemViewModel", "Gemini call failed with code: ${response.code}")
                            ""
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GemViewModel", "Error calling Gemini API: ${e.message}")
                    ""
                }
            }

            _aiDescription.value = if (result.isBlank()) defaultDescription else result
            _isGeneratingAI.value = false
        }
    }

    // Explorer inserts a new crowdsourced spot
    fun addGem(title: String, description: String, category: String = "Scenic") {
        viewModelScope.launch {
            val uploaderId = currentUser.value?.id ?: 1
            val newGem = HiddenGem(
                title = title,
                description = description,
                latitude = _droppedLat.value,
                longitude = _droppedLng.value,
                uploaderId = uploaderId,
                isVerified = false,
                category = category
            )
            val id = repository.insertGem(newGem).toInt()
            _isDroppingPin.value = false
            
            // Automatically select the newly created spot
            selectGem(id)
            setMapCenter(_droppedLat.value, _droppedLng.value)
        }
    }

    // Business Owner claims a spot
    fun claimSpot(gemId: Int) {
        viewModelScope.launch {
            val gem = repository.getGemById(gemId)
            val activeUser = currentUser.value
            if (gem != null && activeUser != null) {
                // Claim it: Set uploaderId to active business user, verify it!
                val claimedGem = gem.copy(
                    isVerified = true,
                    uploaderId = activeUser.id
                )
                repository.updateGem(claimedGem)
                // Reload state
                selectGem(gemId)
            }
        }
    }

    // Business Owner adds promotional activity/experience
    fun addActivity(gemId: Int, name: String, desc: String, schedule: String, priceLevel: Int) {
        viewModelScope.launch {
            val activity = GemActivity(
                gemId = gemId,
                activityName = name,
                description = desc,
                schedule = schedule,
                priceLevel = priceLevel,
                isActive = true
            )
            repository.insertActivity(activity)
            
            // Refresh detail
            selectGem(gemId)
        }
    }

    // Explorer writes a review
    fun addReview(gemId: Int, rating: Int, crowdDensity: Int, comment: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val review = GemReview(
                gemId = gemId,
                userId = user.id,
                username = user.username,
                rating = rating,
                crowdDensity = crowdDensity,
                comment = comment,
                createdAt = System.currentTimeMillis()
            )
            repository.insertReview(review)
            
            // Refresh detail
            selectGem(gemId)
        }
    }

    // Business Owner replies to a review
    fun submitReply(reviewId: Int, replyText: String) {
        viewModelScope.launch {
            val gemId = _selectedGemId.value ?: return@launch
            val reviews = repository.getReviewsForGem(gemId)
            val reviewToReply = reviews.find { it.id == reviewId }
            if (reviewToReply != null) {
                val updatedReview = reviewToReply.copy(
                    businessReply = replyText
                )
                repository.updateReview(updatedReview)
                
                // Refresh detail
                selectGem(gemId)
            }
        }
    }
}
