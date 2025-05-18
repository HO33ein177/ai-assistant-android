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
// import androidx.compose.ui.res.colorResource // Only needed if you still load specific R.color values not in theme
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bio.R // Keep for font resources
import com.example.bio.presentation.common.component.theme.BioTheme // Assuming BioTheme provides customColors
import com.example.bio.presentation.common.component.theme.customColors


private val IconButtonSizeModifier = Modifier.height(50.dp)

@Composable
fun RoundedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    elevation: Dp = 0.dp,
    backGroundColor: Color = MaterialTheme.customColors.lightBlue,
    contentColor: Color = Color.White
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.then(IconButtonSizeModifier),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backGroundColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = elevation,
            pressedElevation = elevation + 2.dp,
            disabledElevation = 0.dp
        )
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
    activeColor: Color = MaterialTheme.customColors.lightBlue,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    val currentContentColor = if (state) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.then(IconButtonSizeModifier),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (state) activeColor else inactiveColor,
            contentColor = currentContentColor
        )
    ) {
        Text(
            text = text,
            style = LocalTextStyle.current.copy(
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.vazirmatn_regular))
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ButtonPreview() {
    BioTheme {
        RoundedButton(text = "Button", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ToggleButtonPreview() {
    BioTheme {
        RoundedToggleButton(state = true, text = "Active Toggle", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ToggleButtonInactivePreview() {
    BioTheme {
        RoundedToggleButton(state = false, text = "Inactive Toggle", onClick = {})
    }
}
