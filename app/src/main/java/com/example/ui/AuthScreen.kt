package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthState
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthScreen(viewModel: GemViewModel) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    val isFirebaseAvailable by viewModel.isFirebaseAvailable.collectAsState()

    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("explorer") } // "explorer" or "business"
    var passwordVisible by remember { mutableStateOf(false) }

    var localError by remember { mutableStateOf<String?>(null) }

    // Clear error on toggle
    LaunchedEffect(isSignUp) {
        localError = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SophisticatedBgBase)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Brand Header
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(SophisticatedPrimary, SophisticatedSecondary)
                        ),
                        RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    tint = SophisticatedBgDark,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = "HIDDEN GEMS",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                fontSize = 28.sp,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Discover off-the-beaten-path secrets & experiences in San Francisco",
                color = SophisticatedTextMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Firebase Integration Status Indicator
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isFirebaseAvailable) 
                        Color(0xFF1E2D24) else Color(0xFF2C221E)
                ),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(
                    1.dp, 
                    if (isFirebaseAvailable) Color(0xFF344D3D) else Color(0xFF4A342C)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isFirebaseAvailable) Icons.Default.VerifiedUser else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (isFirebaseAvailable) SophisticatedSecondary else Color(0xFFE57373),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isFirebaseAvailable) 
                            "Authentic Firebase & Credentials active." 
                            else "Sandbox Fallback active (Running robust in-memory authentication).",
                        color = if (isFirebaseAvailable) SophisticatedSecondary else Color(0xFFE57373),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 14.sp
                    )
                }
            }

            // Tab Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(SophisticatedBgDark, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = { isSignUp = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isSignUp) SophisticatedSurface else Color.Transparent,
                        contentColor = if (!isSignUp) Color.White else SophisticatedTextMuted
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = { isSignUp = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSignUp) SophisticatedSurface else Color.Transparent,
                        contentColor = if (isSignUp) Color.White else SophisticatedTextMuted
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Sign Up", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Input Fields Form
            Card(
                colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SophisticatedBorder.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isSignUp) {
                        // Username Field for Signup
                        Text(
                            text = "Choose Username",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            placeholder = { Text("e.g. SF_Explorer_1", color = Color.Gray, fontSize = 13.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = SophisticatedPrimary,
                                unfocusedBorderColor = SophisticatedBorder,
                                focusedContainerColor = SophisticatedBgDark,
                                unfocusedContainerColor = SophisticatedBgDark
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_username_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Role Selector Field
                        Text(
                            text = "Account Role",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { selectedRole = "explorer" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedRole == "explorer") SophisticatedSecondary.copy(alpha = 0.15f) else Color.Transparent,
                                    contentColor = if (selectedRole == "explorer") SophisticatedSecondary else SophisticatedTextMuted
                                ),
                                border = BorderStroke(
                                    1.dp, 
                                    if (selectedRole == "explorer") SophisticatedSecondary else SophisticatedBorder
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Explore, null, modifier = Modifier.size(16.dp))
                                    Text("Explorer", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            Button(
                                onClick = { selectedRole = "business" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedRole == "business") SophisticatedPrimary.copy(alpha = 0.15f) else Color.Transparent,
                                    contentColor = if (selectedRole == "business") SophisticatedPrimary else SophisticatedTextMuted
                                ),
                                border = BorderStroke(
                                    1.dp, 
                                    if (selectedRole == "business") SophisticatedPrimary else SophisticatedBorder
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Store, null, modifier = Modifier.size(16.dp))
                                    Text("Business", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Email Field
                    Text(
                        text = "Email Address",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("you@example.com", color = Color.Gray, fontSize = 13.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SophisticatedPrimary,
                            unfocusedBorderColor = SophisticatedBorder,
                            focusedContainerColor = SophisticatedBgDark,
                            unfocusedContainerColor = SophisticatedBgDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Password Field
                    Text(
                        text = "Password",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("••••••••", color = Color.Gray, fontSize = 13.sp) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SophisticatedPrimary,
                            unfocusedBorderColor = SophisticatedBorder,
                            focusedContainerColor = SophisticatedBgDark,
                            unfocusedContainerColor = SophisticatedBgDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Error Message Display
            val finalError = localError ?: (if (authState is AuthState.Error) (authState as AuthState.Error).message else null)
            if (finalError != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF421E1E)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF8C3434)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Error, null, tint = Color(0xFFE57373), modifier = Modifier.size(18.dp))
                        Text(
                            text = finalError,
                            color = Color(0xFFE57373),
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Main Action Submit Button
            Button(
                onClick = {
                    localError = null
                    if (email.isBlank() || password.isBlank()) {
                        localError = "Please fill in all email and password fields."
                        return@Button
                    }
                    if (isSignUp) {
                        if (username.isBlank()) {
                            localError = "Please specify a username."
                            return@Button
                        }
                        viewModel.signUpWithEmail(email, password, username, selectedRole)
                    } else {
                        viewModel.loginWithEmail(email, password)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SophisticatedPrimary,
                    contentColor = SophisticatedBgDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("auth_submit_button"),
                enabled = authState !is AuthState.Loading
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        color = SophisticatedBgDark,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isSignUp) "Create Account" else "Sign In Securely",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Alternative Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = SophisticatedBorder.copy(alpha = 0.3f))
                Text("OR", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                HorizontalDivider(modifier = Modifier.weight(1f), color = SophisticatedBorder.copy(alpha = 0.3f))
            }

            // External Authentic Providers Row (Google & Passkeys)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Google Sign In
                Button(
                    onClick = {
                        localError = null
                        viewModel.signInWithGoogle(context, selectedRole)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SophisticatedSurface,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SophisticatedBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("google_signin_button"),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "G",
                                color = Color(0xFF4285F4),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                        Text("Google", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                // Passkey Sign In
                Button(
                    onClick = {
                        localError = null
                        viewModel.signInWithPasskey(context)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SophisticatedSurface,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SophisticatedBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("passkey_signin_button"),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🔑", fontSize = 16.sp)
                        Text("Passkey", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            // Quick Bypass Guest Link
            Button(
                onClick = { viewModel.loginAsGuest() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = SophisticatedSecondary),
                modifier = Modifier.padding(top = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Continue as Guest Explorer →",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
