package co.edu.uniquindio.entorno.core.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    errorMessage: String? = null
) {
    val colors = MaterialTheme.colorScheme

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = colors.onBackground,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                if (placeholder.isNotBlank()) {
                    Text(text = placeholder, color = colors.onSurfaceVariant.copy(alpha = 0.6f))
                }
            },
            leadingIcon = leadingIcon?.let {
                { Icon(imageVector = it, contentDescription = null, tint = colors.onSurfaceVariant) }
            },
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            singleLine = true,
            isError = errorMessage != null,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.outline,
                errorBorderColor = colors.error,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                errorContainerColor = colors.surface
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                fontSize = 12.sp,
                color = colors.error,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}