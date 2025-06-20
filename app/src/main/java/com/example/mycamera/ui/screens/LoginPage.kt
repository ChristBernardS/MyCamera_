// LoginPage.kt
package com.example.mycamera.ui.screens

import android.content.ContentValues.TAG
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource // Impor painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mycamera.R // Pastikan ini diimpor untuk mengakses drawable
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
// Hapus impor ikon Google dari Material Icons Extended
// import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.filled.Google


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPage(navController: NavController) {
    val context = LocalContext.current
    val auth: FirebaseAuth = Firebase.auth
    val db: FirebaseFirestore = Firebase.firestore
    var emailOrUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { idToken ->
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            val firebaseUser = auth.currentUser
                            firebaseUser?.let { user ->
                                db.collection("users").document(user.uid).get()
                                    .addOnSuccessListener { documentSnapshot ->
                                        if (!documentSnapshot.exists()) {
                                            val userData = hashMapOf(
                                                "username" to (user.displayName ?: user.email?.substringBefore("@") ?: "User"),
                                                "email" to user.email,
                                                "userId" to user.uid,
                                                "profilePictureUrl" to (user.photoUrl?.toString() ?: ""),
                                                "followers" to listOf<String>(),
                                                "following" to listOf<String>()
                                            )
                                            db.collection("users").document(user.uid)
                                                .set(userData)
                                                .addOnSuccessListener {
                                                    Log.d(TAG, "User data saved for new Google user: ${user.uid}")
                                                    navController.navigate("home") {
                                                        popUpTo("login") { inclusive = true }
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    errorMessage = "Google sign-in successful, but failed to save user data: ${e.message}"
                                                    Log.e(TAG, "Failed to save new Google user data", e)
                                                }
                                        } else {
                                            Log.d(TAG, "Existing Google user signed in: ${user.uid}")
                                            navController.navigate("home") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        errorMessage = "Failed to check user existence: ${e.message}"
                                        Log.e(TAG, "Error checking Google user existence", e)
                                    }
                            }
                        } else {
                            errorMessage = authTask.exception?.message ?: "Google sign-in failed with Firebase."
                            Log.e(TAG, "Firebase Google auth failed", authTask.exception)
                        }
                    }
            } ?: run {
                errorMessage = "Google ID Token is null."
                Log.e(TAG, "Google ID Token is null.")
            }
        } catch (e: ApiException) {
            Log.w(TAG, "Google sign in failed: ${e.statusCode}", e)
            errorMessage = "Google Sign-In failed: ${e.message}"
        } catch (e: Exception) {
            errorMessage = "An unexpected error occurred during Google Sign-In: ${e.message}"
            Log.e(TAG, "Unexpected error during Google Sign-In", e)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F0E7))
    ) {
        Image(
            painter = painterResource(id = R.drawable.login_top),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .offset(y = (-10).dp),
            contentScale = ContentScale.FillWidth
        )
        Image(
            painter = painterResource(id = R.drawable.login_bottom),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .offset(y = -100.dp),
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
                text = "Login",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4C4C4C),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = emailOrUsername,
                onValueChange = {
                    emailOrUsername = it
                    errorMessage = null
                },
                label = { Text("Email or Username") },
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
                    errorMessage = null
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

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }

            Button(
                onClick = {
                    if (emailOrUsername.isBlank() || password.isBlank()) {
                        errorMessage = "Email/Username and Password cannot be empty."
                    } else {
                        errorMessage = null
                        auth.signInWithEmailAndPassword(emailOrUsername, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    errorMessage = task.exception?.message ?: "Login failed. Please try again."
                                }
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB88C4A)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Login", color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    errorMessage = null
                    signInLauncher.launch(googleSignInClient.signInIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(24.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image( // Menggunakan Image dari drawable
                        painter = painterResource(id = R.drawable.google), // <-- Menggunakan R.drawable.ic_google_logo
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign In with Google",
                        color = Color.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { navController.navigate("register") }) {
                Text("Don't have an account? Register here", color = Color(0xFFB88C4A))
            }
        }
    }
}
