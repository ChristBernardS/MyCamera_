package com.example.mycamera.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mycamera.R // Make sure this is imported to access drawables
import com.google.firebase.auth.FirebaseAuth // Import FirebaseAuth
import com.google.firebase.auth.ktx.auth // Import ktx extension for auth
import com.google.firebase.ktx.Firebase // Import Firebase (for initialization)
import com.google.firebase.firestore.FirebaseFirestore // Import FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore // Import ktx extension for firestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPage(navController: NavController) {
    val auth: FirebaseAuth = Firebase.auth // Get FirebaseAuth instance
    val db: FirebaseFirestore = Firebase.firestore // Get Firestore instance
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) } // State for error messages
    var successMessage by remember { mutableStateOf<String?>(null) } // State for success messages

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F0E7)) // Background color from the image
    ) {
        // Background decoration (abstract lines)
        Image(
            painter = painterResource(id = R.drawable.login_top), // Replace with your image resource
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .offset(y = (-10).dp), // Adjust position
            contentScale = ContentScale.FillWidth
        )
        Image(
            painter = painterResource(id = R.drawable.login_bottom), // Replace with your image resource
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .offset(y = -100.dp), // Adjust position
            contentScale = ContentScale.FillWidth
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Register",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4C4C4C), // Title text color
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null // Clear error message when user starts typing
                    successMessage = null // Clear success message
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFB88C4A),
                    unfocusedBorderColor = Color(0xFFC7C7C7),
                    focusedLabelColor = Color(0xCC9B9A9A),
                    unfocusedLabelColor = Color(0xCC9B9A9A),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    errorMessage = null // Clear error message when user starts typing
                    successMessage = null // Clear success message
                },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFB88C4A),
                    unfocusedBorderColor = Color(0xFFC7C7C7),
                    focusedLabelColor = Color(0xCC9B9A9A),
                    unfocusedLabelColor = Color(0xCC9B9A9A),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null // Clear error message when user starts typing
                    successMessage = null // Clear success message
                },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFB88C4A),
                    unfocusedBorderColor = Color(0xFFC7C7C7),
                    focusedLabelColor = Color(0xCC9B9A9A),
                    unfocusedLabelColor = Color(0xCC9B9A9A),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Display error/success messages if any
            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            successMessage?.let { message ->
                Text(
                    text = message,
                    color = Color.Green, // Green color for success
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = {
                    // Input validation
                    if (email.isBlank() || username.isBlank() || password.isBlank()) {
                        errorMessage = "All fields must be filled."
                        return@Button
                    }
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        errorMessage = "Please enter a valid email address."
                        return@Button
                    }
                    if (password.length < 6) {
                        errorMessage = "Password must be at least 6 characters long."
                        return@Button
                    }

                    errorMessage = null // Reset error
                    successMessage = null // Reset success

                    // Create user with email and password
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Registration successful, get current Firebase user
                                val firebaseUser = auth.currentUser
                                firebaseUser?.let { user ->
                                    // Save additional information (username, followers, following) to Firestore
                                    val userData = hashMapOf(
                                        "username" to username,
                                        "email" to user.email,
                                        "userId" to user.uid,
                                        "followers" to listOf<String>(), // Initialize as empty array
                                        "following" to listOf<String>()  // Initialize as empty array
                                    )
                                    db.collection("users").document(user.uid)
                                        .set(userData)
                                        .addOnSuccessListener {
                                            successMessage = "Registration successful for ${username}! You can now login."
                                            // Optional: navigate back to login after short success
                                            // delay(2000)
                                            // navController.navigate("login")
                                        }
                                        .addOnFailureListener { e ->
                                            errorMessage = "Registration successful, but failed to save user data: ${e.message}"
                                            // Allow login, but indicate an issue with saving additional data
                                        }
                                }
                            } else {
                                // Registration failed
                                errorMessage = task.exception?.message ?: "Registration failed. Please try again."
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB88C4A)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Register", color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { navController.navigate("login") }) {
                Text("Already have an account? Login here.", color = Color(0xFFB88C4A))
            }
        }
    }
}