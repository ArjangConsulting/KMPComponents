package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

/**
 * A single form field: an outlined text field with a label and one line of supporting or error
 * text underneath.
 *
 * When [errorText] is non-null the field is rendered in the error state and [errorText] replaces
 * [supportingText]. Newly shown errors are announced through a polite live region.
 */
@Composable
public fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String? = null,
    supportingText: String? = null,
    errorText: String? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val helperText = errorText ?: supportingText
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        label = { Text(label) },
        placeholder = placeholder?.let { text -> @Composable { Text(text) } },
        trailingIcon = trailingContent,
        supportingText = helperText?.let { text ->
            @Composable {
                Text(
                    text = text,
                    modifier = if (errorText != null) {
                        Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                    } else {
                        Modifier
                    },
                )
            }
        },
        isError = errorText != null,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
    )
}

/**
 * A [FormTextField] for secrets such as API keys and passwords.
 *
 * The value is masked by default and a trailing text button toggles visibility. The revealed
 * state is intentionally *not* saved across configuration changes or process death so a secret is
 * never restored in plain text. Clearing the value also hides it again, including when a form is
 * reset. The keyboard is configured as a password keyboard with autocorrect disabled so the value
 * is not learned by the IME.
 */
@Composable
public fun SecretTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    supportingText: String? = null,
    errorText: String? = null,
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    showLabel: String = "Show",
    hideLabel: String = "Hide",
) {
    var revealed by remember(value.isEmpty()) { mutableStateOf(false) }
    FormTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        enabled = enabled,
        supportingText = supportingText,
        errorText = errorText,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            autoCorrectEnabled = false,
            imeAction = imeAction,
        ),
        keyboardActions = keyboardActions,
        visualTransformation = if (revealed) VisualTransformation.None else PasswordVisualTransformation(),
        trailingContent = {
            TextButton(onClick = { revealed = !revealed }, enabled = enabled) {
                Text(if (revealed) hideLabel else showLabel)
            }
        },
    )
}
