package com.example.bio.presentation.common.component.auth.change_password

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.bio.R
import com.example.bio.presentation.common.component.reusable.MyBasicTextField
import com.example.bio.presentation.common.component.reusable.RoundedButton




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavController,
    viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    // Observe state from the ViewModel
    val email by viewModel.email
    val resetStatus by viewModel.resetStatus
    val context = LocalContext.current
    val isLoading = resetStatus is ResetStatus.Loading

    // Show feedback Toast/Snackbar based on resetStatus changes
    LaunchedEffect(resetStatus) {
        when (val status = resetStatus) {
            is ResetStatus.Success -> {
                Toast.makeText(context, "ایمیل بازنشانی رمز عبور با موفقیت ارسال شد!", Toast.LENGTH_LONG).show()
                viewModel.resetStatusHandled() // Reset status after showing toast
            }
            is ResetStatus.Error -> {
                Toast.makeText(context, status.message, Toast.LENGTH_LONG).show()
                viewModel.resetStatusHandled() // Reset status after showing toast
            }
            else -> { /* Idle or Loading - No immediate feedback needed */ }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("                       بازیابی رمز عبور") })
        },
         containerColor = Color(0xFFE8EAF6),
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 32.dp), // Consistent padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Center content vertically
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp),
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
                    color = colorResource(R.color.purple_500)
                )

                Image(
                    painter = painterResource(id = R.drawable.logo), // logo
                    contentDescription = "logo",
                    modifier = Modifier.size(160.dp)
                )
            }
            Text(
                text = "آدرس ایمیل خود را وارد کنید",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "ما ایمیلی حاوی دستورالعمل های بازیابی رمز عبور برای شما ارسال خواهیم کرد.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Email Input Field
            MyBasicTextField(
                value = email,
                onValueChange = viewModel::onEmailChange, // Use function reference
                label = "آدرس ایمیل",
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                trailingIcon = Icons.Outlined.Email,
                modifier = Modifier.fillMaxWidth(),
                isError = resetStatus is ResetStatus.Error // Show error state if failed
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Loading Indicator or Button
            Box(contentAlignment = Alignment.Center, modifier = Modifier.height(48.dp)) { // Reserve space
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    RoundedButton(
                        text = "ارسال ایمیل بازیابی",
                        onClick = { viewModel.sendPasswordResetEmail() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = email.isNotBlank() // Enable button only if email has text
                    )
                }
            }


            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colorResource(R.color.purple_500) // رنگ متن و ripple
                )
            ) {
                Text("بازگشت به ورود")
            }
        }
    }
}