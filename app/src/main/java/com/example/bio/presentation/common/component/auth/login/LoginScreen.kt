package com.example.bio.presentation.common.component.auth.login


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.bio.AppDestinations
import com.example.bio.R
import com.example.bio.presentation.common.component.reusable.GradientBox
import com.example.bio.presentation.common.component.reusable.MyBasicTextField
import com.example.bio.presentation.common.component.reusable.RoundedButton

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = hiltViewModel(),
) {

    val email = viewModel.email.value
    val password = viewModel.password.value

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
                    color = colorResource(R.color.purple_700)
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
            ) {

                Column(

                    modifier = Modifier
                        .padding(20.dp)

                ) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        text = "ورود به حساب کاربری",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.vazirmatn_bold)),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = colorResource(R.color.black)
                    )

                    Spacer(Modifier.height(16.dp))


                    MyBasicTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        value = email,
                        label = "آدرس ایمیل",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        keyboardActions = KeyboardActions(),
                        trailingIcon = Icons.Outlined.Email,
                        onValueChange = {
                            viewModel.changeEmail(it)
                        }
                    )

                    Spacer(Modifier.height(24.dp))

                    MyBasicTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        value = password,
                        label = "رمز عبور",
                        maxLength = 32,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        keyboardActions = KeyboardActions(),
                        isPassword = true,
                        trailingIcon = Icons.Outlined.Lock,
                        onValueChange = {
                            viewModel.changePassword(it)
                        }
                    )

                    Spacer(Modifier.height(24.dp))

                    RoundedButton(
                        text = "ورود",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        onClick = {
//                            navController.navigate(AppDestinations.LOGIN_ROUTE)
                        }
                    )

                    Row(
                        modifier = Modifier
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Text(
                            modifier = Modifier
                                .clickable {
                                    navController.navigate(AppDestinations.FORGET_PASSWORD_ROUTE)
                                },
                            text = "رمز عبور را فراموش کرده اید؟",
                            color = colorResource(R.color.black),
                            style = LocalTextStyle.current.copy(
                                fontFamily = FontFamily(Font(R.font.vazirmatn_regular))
                            )
                        )

                        Spacer(Modifier.weight(1f))

                        Text(
                            modifier = Modifier.clickable {
                                navController.navigate(AppDestinations.SIGNUP_ROUTE)
                            },
                            text = "ایجاد حساب کاربری",
                            color = colorResource(R.color.black),
                            style = LocalTextStyle.current.copy(
                                fontFamily = FontFamily(Font(R.font.vazirmatn_regular))
                            )
                        )

                    }

                }


            }
        }


    }

}