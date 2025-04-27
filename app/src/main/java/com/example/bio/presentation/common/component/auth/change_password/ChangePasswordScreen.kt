package com.example.bio.presentation.common.component.auth.change_password

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.bio.AppDestinations
import com.example.bio.R
import com.example.bio.presentation.common.component.auth.change_password.components.CodeScreen
import com.example.bio.presentation.common.component.auth.change_password.components.PasswordScreen
import com.example.bio.presentation.common.component.reusable.GradientBox

@Composable
fun ChangePasswordScreen(
    navController: NavController,
    viewModel: ChangePasswordViewModel = hiltViewModel(),
) {

    val isCodeScreen = viewModel.isCodeScreen.value
    val email = viewModel.email.value
    val code = viewModel.code.value
    val password = viewModel.password.value
    val rePassword = viewModel.rePassword.value

    Scaffold {

        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    modifier = Modifier
                        .padding(top = 16.dp),
                    text = "Soundwave",
                    style = LocalTextStyle.current.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp
                    ),
                    color = colorResource(R.color.main_blue)
                )

                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "logo",
                    modifier = Modifier
                        .size(160.dp)
                )

            }

            GradientBox(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1.3f)
                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
//                modifier = Modifier
//                    // .fillMaxSize() // Remove these temporarily
//                    // .weight(1.3f)
//                    .fillMaxWidth() // Keep width
//                    .height(400.dp) // Give explicit height
//                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
            ) {

                if (isCodeScreen) {

                    CodeScreen(
                        email,
                        viewModel,
                        code,
                        onGoToLoginScreen = {
                            navController.navigate(AppDestinations.LOGIN_ROUTE)
                        })

                } else {

                    PasswordScreen(password, viewModel, rePassword)

                }

            }
        }


    }

}

