package com.example.bio.presentation.common.component.reusable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bio.R

@Composable
fun MyBasicTextField(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    maxLength: Int? = null,
    singleLine: Boolean = true,
    isEnabled: Boolean = true,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    readOnly: Boolean = false,
    focusRequester: FocusRequester? = null,
    height: Dp = 48.dp,
    trailingIcon: ImageVector? = null,
    onValueChange: (text: String) -> Unit,
    onTextFieldClick: () -> Unit = {},
    isPassword: Boolean = false, // پارامتر جدید برای تشخیص فیلد رمز عبور
    isHashtag: Boolean = false,
) {
    // کنترلر کیبورد
    val keyboardController = LocalSoftwareKeyboardController.current
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        focusRequester?.requestFocus() // فوکوس روی TextField
        keyboardController?.show() // نمایش کیبورد
    }

    Column(modifier = modifier) {
        BasicTextField(
            value = value,
            onValueChange = { newText ->
                if (maxLength == null || newText.length <= maxLength) {
                    onValueChange(newText)
                }
            },
            enabled = isEnabled,
            singleLine = singleLine,
            textStyle = LocalTextStyle.current.copy(
                color = colorResource(R.color.black),
                fontFamily = FontFamily(Font(R.font.vazirmatn_regular))
                ), // رنگ متن hint
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            readOnly = readOnly,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier
                .then(
                    if (focusRequester != null) {
                        Modifier
                            .focusRequester(focusRequester) // اتصال FocusRequester
                    } else
                        Modifier
                )
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        keyboardController?.show() // نمایش کیبورد هنگام فوکوس
                    }
                },

            ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .height(height)
                    .background(colorResource(R.color.white))
                    .then(
                        if (readOnly) {
                            Modifier.clickable {
                                onTextFieldClick()
                            }
                        } else
                            Modifier
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (isHashtag) {
                    Text(
                        modifier = Modifier
                            .padding(8.dp),
                        text = "#",
                        color = colorResource(R.color.black),
                        style = LocalTextStyle.current.copy(
                            fontSize = 26.sp,
                            fontFamily = FontFamily(Font(R.font.vazirmatn_regular))
                        )
                    )
                } else if (trailingIcon != null) {
                    Icon(
                        modifier = Modifier
                            .padding(8.dp),
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = colorResource(R.color.black)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // نمایش placeholder اگر مقدار value خالی باشد
                    if (value.isEmpty()) {
                        Text(
                            text = label,
                            color = Color.Gray, // رنگ متن placeholder
                            style = LocalTextStyle.current.copy(fontSize = 16.sp)
                        )
                    }
                    it.invoke()
                }

                if (isPassword) {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            modifier = Modifier
                                .padding(8.dp),
                            imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = colorResource(R.color.black)
                        )
                    }
                }

            }
        }
    }
}
