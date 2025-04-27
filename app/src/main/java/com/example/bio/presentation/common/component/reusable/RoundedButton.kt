package com.example.bio.presentation.common.component.reusable

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bio.R


private val IconButtonSizeModifier = Modifier.height(50.dp)

@Composable
fun RoundedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    elevation: Dp = 4.dp,
    backGroundColor: Color = colorResource(R.color.purple_700),
    contentColor: Color = colorResource(R.color.black),
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier then IconButtonSizeModifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backGroundColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(elevation)
    ) {
        Text(
            text = text,
            style = LocalTextStyle.current.copy(
                fontSize = 18.sp,
                color = contentColor,
                fontFamily = FontFamily(Font(R.font.vazirmatn_bold))
            ),
        )
    }
}

@Composable
fun RoundedToggleButton(
    state: Boolean,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = colorResource(R.color.main_blue),
    inactiveColor: Color = colorResource(R.color.white),
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier then IconButtonSizeModifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (state) activeColor else inactiveColor,
            contentColor = colorResource(R.color.black),
        )
    ) {
        Text(
            text = text,
            color = colorResource(R.color.black),
            style = LocalTextStyle.current.copy(
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.vazirmatn_regular))
            ),
        )
    }
}

@Preview
@Composable
private fun ButtonPreview() {
    MaterialTheme {
        RoundedButton(text = "Button", onClick = {})
    }
}