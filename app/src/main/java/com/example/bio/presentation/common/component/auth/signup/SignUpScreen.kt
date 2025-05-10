package com.example.bio.presentation.common.component.auth.signup // Correct package



import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bio.R
import com.example.bio.data.local.dao.UserDao
import com.example.bio.data.local.entity.User
import com.example.bio.presentation.common.component.reusable.MyBasicTextField
import com.example.bio.presentation.common.component.reusable.RoundedButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// Define Signup states
sealed interface SignupResult {
    data object Idle : SignupResult
    data object Loading : SignupResult
    // Pass the local DB user ID on success
    data class Success(val userId: Long) : SignupResult
    data class Error(val message: String) : SignupResult
}

private const val TAG = "SignupScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    navController: NavController,
    // Callback to notify MainActivity/AppNavigation about success
    // Pass the local database user ID (Long) after successful creation
    onSignupSuccess: (Long) -> Unit
) {
    // --- State Management ---
    var name by remember { mutableStateOf("") } // Add name state if needed
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var signupStatus by remember { mutableStateOf<SignupResult>(SignupResult.Idle) }
    val isLoading = signupStatus is SignupResult.Loading

    // --- Coroutine Scope & Context ---
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // --- Get Dependencies (FirebaseAuth and UserDao) ---
    // Since we might not have a ViewModel here, we get dependencies via Hilt EntryPoint
    val hiltEntryPoint = EntryPointAccessors.fromActivity(
        context as androidx.activity.ComponentActivity, // Assuming context is from an Activity
        SignupScreenEntryPoint::class.java
    )
    val firebaseAuth = hiltEntryPoint.getFirebaseAuth()
    val userDao = hiltEntryPoint.getUserDao()

    // --- UI Feedback ---
    LaunchedEffect(signupStatus) {
        when (val status = signupStatus) {
            is SignupResult.Success -> {
                Toast.makeText(context, "Signup Successful!", Toast.LENGTH_SHORT).show()
                onSignupSuccess(status.userId) // Call the callback with the local DB ID
            }
            is SignupResult.Error -> {
                Toast.makeText(context, status.message, Toast.LENGTH_LONG).show()
                signupStatus = SignupResult.Idle // Reset status after showing error
            }
            else -> {} // Idle or Loading
        }
    }

    // --- Signup Logic ---
    fun attemptSignup() {
        // Basic Validation
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank() || name.isBlank()) {
            signupStatus = SignupResult.Error("Please fill in all fields.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signupStatus = SignupResult.Error("Invalid email format.")
            return
        }
        if (password != confirmPassword) {
            signupStatus = SignupResult.Error("Passwords do not match.")
            return
        }
        // Add password strength check if desired

        signupStatus = SignupResult.Loading

        coroutineScope.launch {
            try {
                // 1. Create user in Firebase Authentication
                Log.d(TAG, "Attempting Firebase user creation...")
                val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                Log.d(TAG, "Firebase user created successfully: UID=${firebaseUser?.uid}")

                if (firebaseUser != null) {
                    // 2. Create user in local Room database (WITHOUT password)
                    // Use Firebase UID as a unique identifier if needed later,
                    // or just store basic info. Here we store email and name.
                    // IMPORTANT: Do NOT store the plain password locally.
                    // We don't store the Firebase password hash either, as Firebase handles auth.
                    val localUser = User(
                        // id will be auto-generated by Room
                        email = email,
                        password = "", // Store empty string or null for password locally
                        name = name,
                        firebaseUid = firebaseUser.uid // Optional: Store Firebase UID
                    )

                    Log.d(TAG, "Attempting local DB user insertion...")
                    // Insert into Room on a background thread
                    val insertedUserId = withContext(Dispatchers.IO) {
                        userDao.insert(localUser) // Assuming insert returns the new row ID (Long)
                    }
                    Log.d(TAG, "Local DB user inserted with ID: $insertedUserId")

                    // 3. Report Success with the LOCAL Database ID
                    signupStatus = SignupResult.Success(insertedUserId)

                } else {
                    // Should not happen if createUserWithEmailAndPassword succeeds, but handle defensively
                    Log.e(TAG, "Firebase user was null after successful creation task.")
                    signupStatus = SignupResult.Error("Signup failed: Could not get user details.")
                }

            } catch (e: FirebaseAuthWeakPasswordException) {
                Log.w(TAG, "Signup failed: Weak password", e)
                signupStatus = SignupResult.Error("Password is too weak (at least 6 characters).")
            } catch (e: FirebaseAuthUserCollisionException) {
                Log.w(TAG, "Signup failed: Email already in use", e)
                signupStatus = SignupResult.Error("Email address is already registered.")
            } catch (e: Exception) { // Catch other exceptions (network, DB insert, etc.)
                Log.e(TAG, "Signup failed", e)
                signupStatus = SignupResult.Error("Signup failed: ${e.localizedMessage}")
            }
        }
    }

    // --- UI ---
    Scaffold(
        topBar = { TopAppBar(title = { Text("ایجاد حساب کاربری") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f), // Keep layout structure
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // --- Logo and Title ---
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "Soundwave",
                    style = LocalTextStyle.current.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp
                    ),
                    // Use a color defined in your resources or theme
                    color = colorResource(R.color.purple_700) // Example, adjust as needed
                )

                Image(
                    painter = painterResource(id = R.drawable.logo), // Your logo
                    contentDescription = "logo",
                    modifier = Modifier.size(160.dp)
                )
            }

            // Add fields for Name, Email, Password, Confirm Password
            MyBasicTextField(
                value = name,
                onValueChange = { name = it },
                label = "نام",
                trailingIcon = Icons.Outlined.Person,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            MyBasicTextField(
                value = email,
                onValueChange = { email = it },
                label = "آدرس ایمیل",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                trailingIcon = Icons.Outlined.Email,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            MyBasicTextField(
                value = password,
                onValueChange = { password = it },
                label = "رمز عبور",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                trailingIcon = Icons.Outlined.Lock,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            MyBasicTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "تکرار رمز عبور",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                trailingIcon = Icons.Outlined.Lock,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                RoundedButton(
                    text = "ثبت نام",
                    onClick = { attemptSignup() },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colorResource(R.color.purple_700) // رنگ متن و ripple
                )
            ) {
                Text("حساب کاربری دارید؟ ورود")
            }
        }
    }
}

// --- Hilt EntryPoint to get dependencies in Composable ---
// Define this outside the Composable function, usually at the top level of the file
@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.android.components.ActivityComponent::class)
interface SignupScreenEntryPoint {
    fun getFirebaseAuth(): FirebaseAuth
    fun getUserDao(): UserDao
}
