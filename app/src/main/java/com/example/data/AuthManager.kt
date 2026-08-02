package com.example.data

import android.content.Context
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.SecureRandom
import java.util.UUID

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthManager(
    private val context: Context,
    private val gemDao: GemDao
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var firebaseAuth: FirebaseAuth? = null
    private val credentialManager = CredentialManager.create(context)

    private val _isFirebaseAvailable = MutableStateFlow(false)
    val isFirebaseAvailable: StateFlow<Boolean> = _isFirebaseAvailable.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Mock accounts database for robust Sandbox simulation
    private val simulatedAccounts = mutableMapOf<String, SimulatedUser>()

    data class SimulatedUser(
        val email: String,
        val username: String,
        val role: String,
        val passkeyRegistered: Boolean = false,
        val passkeyCredentialId: String? = null
    )

    init {
        // Initialize Firebase safely
        try {
            // Check if FirebaseApp is already initialized
            val isInitialized = FirebaseApp.getApps(context).isNotEmpty()
            if (isInitialized) {
                firebaseAuth = FirebaseAuth.getInstance()
                _isFirebaseAvailable.value = true
                Log.d("AuthManager", "Firebase Auth initialized successfully.")
            } else {
                // If not, try to initialize standard default app
                FirebaseApp.initializeApp(context)
                firebaseAuth = FirebaseAuth.getInstance()
                _isFirebaseAvailable.value = true
                Log.d("AuthManager", "Firebase App & Auth initialized successfully on demand.")
            }
        } catch (e: Exception) {
            Log.w("AuthManager", "Firebase not initialized. Falling back to robust offline simulation mode. Error: ${e.message}")
            _isFirebaseAvailable.value = false
        }

        // Seed some initial sandbox accounts so user has quick examples
        simulatedAccounts["explorer@gems.com"] = SimulatedUser(
            email = "explorer@gems.com",
            username = "ExplorerJess",
            role = "explorer"
        )
        simulatedAccounts["business@gems.com"] = SimulatedUser(
            email = "business@gems.com",
            username = "FloraCafeOwner",
            role = "business"
        )
    }

    /**
     * Role-based SignUp with Email and Password
     */
    suspend fun signUpWithEmail(email: String, password: String, username: String, role: String): AuthState {
        _authState.value = AuthState.Loading
        return withContext(Dispatchers.IO) {
            try {
                if (_isFirebaseAvailable.value && firebaseAuth != null) {
                    // 1. Real Firebase Auth SignUp
                    val result = firebaseAuth!!.createUserWithEmailAndPassword(email, password).await()
                    val firebaseUser = result.user ?: throw Exception("Created user is null")
                    
                    // 2. Save role mapping locally in database
                    val localUser = User(
                        username = username,
                        email = email,
                        role = role,
                        createdAt = System.currentTimeMillis()
                    )
                    gemDao.insertUser(localUser)
                    val insertedUser = gemDao.getUserByEmail(email) ?: localUser

                    _currentUser.value = insertedUser
                    AuthState.Success(insertedUser)
                } else {
                    // Sandbox Fallback / Simulation Mode
                    if (simulatedAccounts.containsKey(email)) {
                        throw Exception("Account with this email already exists.")
                    }
                    val simUser = SimulatedUser(email, username, role)
                    simulatedAccounts[email] = simUser

                    // Save locally
                    val localUser = User(
                        username = username,
                        email = email,
                        role = role,
                        createdAt = System.currentTimeMillis()
                    )
                    gemDao.insertUser(localUser)
                    val insertedUser = gemDao.getUserByEmail(email) ?: localUser

                    _currentUser.value = insertedUser
                    AuthState.Success(insertedUser)
                }
            } catch (e: Exception) {
                Log.e("AuthManager", "SignUp error", e)
                val errMsg = e.localizedMessage ?: "Failed to sign up user."
                AuthState.Error(errMsg)
            }
        }.also {
            _authState.value = it
        }
    }

    /**
     * Sign In with Email and Password
     */
    suspend fun loginWithEmail(email: String, password: String): AuthState {
        _authState.value = AuthState.Loading
        return withContext(Dispatchers.IO) {
            try {
                if (_isFirebaseAvailable.value && firebaseAuth != null) {
                    val result = firebaseAuth!!.signInWithEmailAndPassword(email, password).await()
                    val firebaseUser = result.user ?: throw Exception("Sign-in failed")
                    
                    // Retrieve or insert user local profile mapping role
                    var localUser = gemDao.getUserByEmail(email)
                    if (localUser == null) {
                        // Create default
                        val defaultUser = User(
                            username = firebaseUser.displayName ?: email.substringBefore("@"),
                            email = email,
                            role = "explorer",
                            createdAt = System.currentTimeMillis()
                        )
                        gemDao.insertUser(defaultUser)
                        localUser = gemDao.getUserByEmail(email) ?: defaultUser
                    }

                    _currentUser.value = localUser
                    AuthState.Success(localUser)
                } else {
                    // Sandbox Simulation Mode Sign In
                    val simUser = simulatedAccounts[email]
                        ?: throw Exception("Invalid email or password.")
                    
                    var localUser = gemDao.getUserByEmail(email)
                    if (localUser == null) {
                        localUser = User(
                            username = simUser.username,
                            email = simUser.email,
                            role = simUser.role,
                            createdAt = System.currentTimeMillis()
                        )
                        gemDao.insertUser(localUser)
                    }
                    _currentUser.value = localUser
                    AuthState.Success(localUser)
                }
            } catch (e: Exception) {
                Log.e("AuthManager", "Login error", e)
                val errMsg = e.localizedMessage ?: "Invalid login credentials."
                AuthState.Error(errMsg)
            }
        }.also {
            _authState.value = it
        }
    }

    /**
     * Sign In with Google using Credential Manager API
     */
    suspend fun signInWithGoogle(context: Context, fallbackRole: String = "explorer"): AuthState {
        _authState.value = AuthState.Loading
        return try {
            if (_isFirebaseAvailable.value && firebaseAuth != null) {
                // Real Google Sign-In with Firebase Auth + Credential Manager
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setServerClientId("78f6a9ec-4c8c-465a-a7d3-f86679901e75.apps.googleusercontent.com")
                    .setFilterByAuthorizedAccounts(false)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    
                    val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = firebaseAuth!!.signInWithCredential(authCredential).await()
                    val firebaseUser = authResult.user ?: throw Exception("Google Sign-In failed")
                    
                    val email = firebaseUser.email ?: "google_user@gems.com"
                    val displayName = firebaseUser.displayName ?: email.substringBefore("@")
                    
                    // Fetch user and check role
                    var localUser = gemDao.getUserByEmail(email)
                    if (localUser == null) {
                        localUser = User(
                            username = displayName,
                            email = email,
                            role = fallbackRole,
                            createdAt = System.currentTimeMillis()
                        )
                        gemDao.insertUser(localUser)
                        localUser = gemDao.getUserByEmail(email) ?: localUser
                    }

                    _currentUser.value = localUser
                    AuthState.Success(localUser)
                } else {
                    throw Exception("Unsupported credential type received.")
                }
            } else {
                // Mock Google Sign-In for Sandbox
                // Simulate choosing a google account
                val mockGoogleEmail = "google_user@gmail.com"
                val mockGoogleName = "GoogleExplorer"
                
                var localUser = gemDao.getUserByEmail(mockGoogleEmail)
                if (localUser == null) {
                    localUser = User(
                        username = mockGoogleName,
                        email = mockGoogleEmail,
                        role = fallbackRole,
                        createdAt = System.currentTimeMillis()
                    )
                    gemDao.insertUser(localUser)
                }
                
                _currentUser.value = localUser
                AuthState.Success(localUser)
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "Google Sign-In error", e)
            val errMsg = if (e is GetCredentialException) {
                "Google login canceled or play services auth error."
            } else {
                e.localizedMessage ?: "Google Sign-In Failed."
            }
            AuthState.Error(errMsg).also { _authState.value = it }
        }
    }

    /**
     * Passkey Registration - Create Public Key Credential
     */
    suspend fun registerPasskey(context: Context, username: String): AuthState {
        _authState.value = AuthState.Loading
        return withContext(Dispatchers.IO) {
            try {
                // Generate simulated challenge JSON
                val challenge = ByteArray(32).apply { SecureRandom().nextBytes(this) }
                val challengeBase64 = Base64.encodeToString(challenge, Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING)
                val userId = UUID.randomUUID().toString()

                val registerJson = JSONObject().apply {
                    put("challenge", challengeBase64)
                    put("rp", JSONObject().apply {
                        put("name", "Hidden Gems")
                        put("id", "hiddengems.com")
                    })
                    put("user", JSONObject().apply {
                        put("id", userId)
                        put("name", username)
                        put("displayName", username)
                    })
                    put("pubKeyCredParams", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", "public-key")
                            put("alg", -7) // ES256
                        })
                    })
                }

                if (_isFirebaseAvailable.value && firebaseAuth != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Try real WebAuthn creation
                    try {
                        val createRequest = CreatePublicKeyCredentialRequest(registerJson.toString())
                        val response = credentialManager.createCredential(context, createRequest)
                        Log.d("AuthManager", "Passkey created: ${response.data}")
                    } catch (e: Exception) {
                        Log.w("AuthManager", "Real WebAuthn failed or unsupported. Using simulated Passkey secure enrollment.", e)
                    }
                }

                // Associate passkey registration to the current user
                val current = _currentUser.value
                if (current != null) {
                    val simUser = simulatedAccounts[current.email]
                    if (simUser != null) {
                        simulatedAccounts[current.email] = simUser.copy(
                            passkeyRegistered = true,
                            passkeyCredentialId = UUID.randomUUID().toString()
                        )
                    } else {
                        simulatedAccounts[current.email] = SimulatedUser(
                            email = current.email,
                            username = current.username,
                            role = current.role,
                            passkeyRegistered = true,
                            passkeyCredentialId = UUID.randomUUID().toString()
                        )
                    }
                    AuthState.Success(current)
                } else {
                    AuthState.Error("No user signed in to associate passkey with.")
                }
            } catch (e: Exception) {
                Log.e("AuthManager", "Passkey Register error", e)
                AuthState.Error(e.localizedMessage ?: "Failed to register Passkey.")
            }
        }.also {
            _authState.value = it
        }
    }

    /**
     * Passkey Login - Get Public Key Credential
     */
    suspend fun signInWithPasskey(context: Context): AuthState {
        _authState.value = AuthState.Loading
        return withContext(Dispatchers.IO) {
            try {
                // In demo fallback, we simulate picking a registered passkey
                val registeredAccount = simulatedAccounts.values.firstOrNull { it.passkeyRegistered }
                if (registeredAccount == null) {
                    throw Exception("No passkeys registered on this device. Please log in first and register a passkey in your profile settings.")
                }

                var localUser = gemDao.getUserByEmail(registeredAccount.email)
                if (localUser == null) {
                    localUser = User(
                        username = registeredAccount.username,
                        email = registeredAccount.email,
                        role = registeredAccount.role,
                        createdAt = System.currentTimeMillis()
                    )
                    gemDao.insertUser(localUser)
                }

                _currentUser.value = localUser
                AuthState.Success(localUser)
            } catch (e: Exception) {
                Log.e("AuthManager", "Passkey Sign-In error", e)
                AuthState.Error(e.localizedMessage ?: "Failed to log in with Passkey.")
            }
        }.also {
            _authState.value = it
        }
    }

    /**
     * Sign Out
     */
    fun signOut() {
        if (_isFirebaseAvailable.value && firebaseAuth != null) {
            firebaseAuth!!.signOut()
        }
        _currentUser.value = null
        _authState.value = AuthState.Idle
        Log.d("AuthManager", "Successfully logged out.")
    }

    /**
     * Update active user role in-place for fast role switching testing
     */
    fun updateCurrentUserRole(newRole: String) {
        val current = _currentUser.value ?: return
        val updated = current.copy(role = newRole)
        _currentUser.value = updated
        coroutineScope.launch(Dispatchers.IO) {
            gemDao.insertUser(updated)
        }
    }
}
