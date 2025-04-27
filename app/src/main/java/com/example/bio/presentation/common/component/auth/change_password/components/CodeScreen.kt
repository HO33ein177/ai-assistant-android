package com.example.bio.presentation.common.component.auth.change_password.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bio.R
import com.example.bio.presentation.common.component.auth.change_password.ChangePasswordViewModel
import com.example.bio.presentation.common.component.reusable.MyBasicTextField
import com.example.bio.presentation.common.component.reusable.RoundedButton
import com.example.bio.presentation.common.component.reusable.RoundedToggleButton

@Composable
fun CodeScreen(
    email: String,
    viewModel: ChangePasswordViewModel,
    code: String,
    onGoToLoginScreen: () -> Unit,
) {
    Column(

        modifier = Modifier
            .padding(20.dp)

    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp),
            text = "بازیابی رمز عبور",
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            MyBasicTextField(
                modifier = Modifier
                    .weight(1.25f),
                value = code,
                label = "رمز ارسال شده",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                keyboardActions = KeyboardActions(),
                isHashtag = true,
                onValueChange = {
                    viewModel.changeCode(it)
                }
            )

            Spacer(Modifier.weight(0.1f))

            RoundedToggleButton(
                modifier = Modifier
                    .weight(1f),
                state = false,
                text = "دریافت رمز",
                onClick = {

                }
            )

        }

        Spacer(Modifier.height(24.dp))

        RoundedButton(
            text = "ادامه",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            onClick = {
                viewModel.changeIsCodeScreen(false)
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
                        onGoToLoginScreen()
                    },
                text = "ورود به حساب کاربری",
                color = colorResource(R.color.main_blue),
                style = LocalTextStyle.current.copy(
                    fontFamily = FontFamily(Font(R.font.vazirmatn_regular))
                )
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = "ارتباط با ما",
                color = colorResource(R.color.main_blue),
                style = LocalTextStyle.current.copy(
                    fontFamily = FontFamily(Font(R.font.vazirmatn_regular))
                )
            )

        }

    }
}