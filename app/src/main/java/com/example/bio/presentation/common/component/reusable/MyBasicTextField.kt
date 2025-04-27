package com.example.bio.presentation.common.component.reusable // Adjust package if needed

// --- Add necessary imports ---
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Import your specific resources if needed (e.g., fonts, colors)
// import com.example.bio.R

@Composable
fun MyBasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholderText: String = "", // Optional placeholder separate from label
    leadingIcon: ImageVector? = null, // Changed type to ImageVector
    trailingIcon: ImageVector? = null, // Changed type to ImageVector
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isPassword: Boolean = false,
    maxLength: Int? = null, // Optional max length
    // --- ADD isError parameter ---
    isError: Boolean = false,
    // Add other parameters like isHashtag if needed
    isHashtag: Boolean = false // Example if needed from CodeScreen usage
) {
    // --- Visual Transformation for Password ---
    val visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None

    // --- Define Colors based on Error State ---
    val containerColor = MaterialTheme.colorScheme.surfaceVariant
    val unfocusedIndicatorColor = Color.Transparent
    // Change indicator color on error
    val focusedIndicatorColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val cursorColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val labelColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    val textColor = if (isError) MaterialTheme.colorScheme.error else LocalContentColor.current // Or MaterialTheme.colorScheme.onSurface

    // Using OutlinedTextField as a base is often easier for styling label/placeholder/icons/errors
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Apply maxLength constraint if provided
            if (maxLength == null || newValue.length <= maxLength) {
                onValueChange(newValue)
            }
        },
        modifier = modifier.height(55.dp), // Adjust height as needed
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = textColor), // Apply text color
        label = { Text(label, color = labelColor) }, // Use label parameter
        placeholder = { Text(placeholderText) }, // Use placeholder parameter
        leadingIcon = leadingIcon?.let {
            { Icon(imageVector = it, contentDescription = null, tint = labelColor) }
        },
        trailingIcon = trailingIcon?.let {
            { Icon(imageVector = it, contentDescription = null, tint = labelColor) }
        },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        shape = RoundedCornerShape(12.dp), // Consistent shape
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            disabledContainerColor = containerColor,
            cursorColor = cursorColor,
            focusedBorderColor = focusedIndicatorColor, // Use error color for border when focused
            unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error else unfocusedIndicatorColor, // Use error color for border when unfocused
            errorBorderColor = MaterialTheme.colorScheme.error, // Explicit error border color
            // Adjust other colors as needed
            focusedLabelColor = labelColor,
            unfocusedLabelColor = labelColor,
        ),
        isError = isError // Pass the isError state to OutlinedTextField for default error handling (like border color)
    )

    /*
    // --- Original BasicTextField Implementation (Harder to style errors/labels) ---
    BasicTextField(
        value = value,
        onValueChange = { newValue ->
             if (maxLength == null || newValue.length <= maxLength) {
                onValueChange(newValue)
            }
        },
        modifier = modifier
            .background(containerColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp), // Padding inside the background
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = textColor),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        visualTransformation = visualTransformation,
        cursorBrush = SolidColor(cursorColor),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxHeight() // Fill height for vertical alignment
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = labelColor, // Use label color for icon tint
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    // Show placeholder if value is empty
                    if (value.isEmpty() && placeholderText.isNotEmpty()) {
                        Text(
                            text = placeholderText,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }
                    // Show label if placeholder isn't shown (or handle label positioning differently)
                    else if (value.isEmpty() && label.isNotEmpty()) {
                         Text(
                             text = label,
                             color = labelColor,
                             fontSize = 16.sp
                         )
                    }
                    innerTextField() // The actual text input area
                }
                if (trailingIcon != null) {
                     Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = labelColor,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    )
    */
}
