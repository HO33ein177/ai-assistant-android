package com.example.bio.presentation.common.component.auth.change_password.components

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
import androidx.compose.material.icons.outlined.Lock
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
import com.example.bio.presentation.common.component.reusable.MyBasicTextField
import com.example.bio.presentation.common.component.reusable.RoundedButton
import com.example.bio.R
import com.example.bio.presentation.common.component.auth.change_password.ChangePasswordViewModel

@Composable
fun PasswordScreen(
    password: String,
    viewModel: ChangePasswordViewModel,
    rePassword: String,
) {
    Column(

        modifier = Modifier
            .padding(20.dp)

    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp),
            text = "تغییر رمز عبور",
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

        MyBasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            value = rePassword,
            label = "تکرار رمز عبور",
            maxLength = 32,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            keyboardActions = KeyboardActions(),
            isPassword = true,
            trailingIcon = Icons.Outlined.Lock,
            onValueChange = {
                viewModel.changeRePassword(it)
            }
        )

        Spacer(Modifier.height(24.dp))

        RoundedButton(
            text = "ورود",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            onClick = {

            }
        )

        Row(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "ایجاد حساب کاربری",
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