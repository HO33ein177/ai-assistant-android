package com.example.bio.presentation.common.component.reusable // Adjust package if needed

// --- Add necessary imports ---

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp


@Composable
fun MyBasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    trailingIcon: ImageVector? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
) {


    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        trailingIcon = trailingIcon?.let {
            { Icon(imageVector = it, contentDescription = label ?: "Trailing Icon") }
        },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        isError = isError,
        singleLine = singleLine,
        maxLines = maxLines,
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
fun TextFieldExample() {
    var textState by remember { mutableStateOf("") }
    var passwordState by remember { mutableStateOf("") }
    var emailState by remember { mutableStateOf("") }
    val isEmailError = !android.util.Patterns.EMAIL_ADDRESS.matcher(emailState).matches() && emailState.isNotEmpty()

    Column(modifier = Modifier.padding(16.dp)) {
        MyBasicTextField(
            value = textState,
            onValueChange = { textState = it },
            label = "Simple Text Field",
            placeholder = "Enter some text"
        )
        Spacer(modifier = Modifier.height(16.dp))
        MyBasicTextField(
            value = passwordState,
            onValueChange = { passwordState = it },
            label = "Password Field",
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = Icons.Outlined.Lock
        )
        Spacer(modifier = Modifier.height(16.dp))
        MyBasicTextField(
            value = emailState,
            onValueChange = { emailState = it },
            label = "Email Field",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            trailingIcon = Icons.Outlined.Email,
            isError = isEmailError
        )
        if (isEmailError) {
            Text("Invalid email format", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
