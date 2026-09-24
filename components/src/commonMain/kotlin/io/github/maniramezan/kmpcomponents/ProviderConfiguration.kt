package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

/**
 * Connection settings for an authenticated service such as an AI provider.
 *
 * [toString] redacts [apiKey] so the state can be logged or included in crash reports safely.
 */
@Immutable
public data class ProviderConfigurationState(
    public val providerName: String = "",
    public val baseUrl: String = "",
    public val model: String = "",
    public val apiKey: String = "",
) {
    override fun toString(): String =
        "ProviderConfigurationState(providerName=$providerName, baseUrl=$baseUrl, model=$model, " +
            "apiKey=${if (apiKey.isEmpty()) "" else REDACTED})"

    private companion object {
        const val REDACTED = "<redacted>"
    }
}

public enum class ProviderConfigurationField { PROVIDER_NAME, BASE_URL, MODEL, API_KEY }

public enum class ProviderConfigurationError { REQUIRED, INVALID_URL }

private val HTTP_URL = Regex("^https?://[^\\s/?#]+([/?#]\\S*)?$", RegexOption.IGNORE_CASE)

/**
 * Validates every field and returns the first problem found per field; an empty map means valid.
 *
 * All fields are required except [ProviderConfigurationState.apiKey] when [apiKeyRequired] is
 * false. [ProviderConfigurationState.baseUrl] must be an absolute `http` or `https` URL.
 * Surrounding whitespace is ignored.
 */
public fun ProviderConfigurationState.validate(
    apiKeyRequired: Boolean = true,
): Map<ProviderConfigurationField, ProviderConfigurationError> = buildMap {
    if (providerName.isBlank()) put(ProviderConfigurationField.PROVIDER_NAME, ProviderConfigurationError.REQUIRED)
    when {
        baseUrl.isBlank() -> put(ProviderConfigurationField.BASE_URL, ProviderConfigurationError.REQUIRED)
        !HTTP_URL.matches(baseUrl.trim()) ->
            put(ProviderConfigurationField.BASE_URL, ProviderConfigurationError.INVALID_URL)
    }
    if (model.isBlank()) put(ProviderConfigurationField.MODEL, ProviderConfigurationError.REQUIRED)
    if (apiKeyRequired && apiKey.isBlank()) {
        put(ProviderConfigurationField.API_KEY, ProviderConfigurationError.REQUIRED)
    }
}

public fun ProviderConfigurationState.isValid(apiKeyRequired: Boolean = true): Boolean =
    validate(apiKeyRequired).isEmpty()

/** Localizable strings for [ProviderConfigurationForm]. */
@Immutable
public data class ProviderConfigurationLabels(
    public val providerName: String = "Provider name",
    public val baseUrl: String = "Base URL",
    public val model: String = "Model",
    public val apiKey: String = "API key",
    public val apiKeyOptional: String = "API key (optional)",
    public val requiredError: String = "Required",
    public val invalidUrlError: String = "Enter a URL starting with http:// or https://",
    public val showSecret: String = "Show",
    public val hideSecret: String = "Hide",
) {
    public fun messageFor(error: ProviderConfigurationError): String = when (error) {
        ProviderConfigurationError.REQUIRED -> requiredError
        ProviderConfigurationError.INVALID_URL -> invalidUrlError
    }
}

/**
 * Stateless form for [ProviderConfigurationState].
 *
 * Validation messages are hidden until [showValidationErrors] is true, typically after the user
 * first tries to submit. The IME advances field by field and the last field's Done action invokes
 * [onDone].
 */
@Composable
public fun ProviderConfigurationForm(
    state: ProviderConfigurationState,
    onStateChange: (ProviderConfigurationState) -> Unit,
    modifier: Modifier = Modifier,
    apiKeyRequired: Boolean = true,
    enabled: Boolean = true,
    showValidationErrors: Boolean = false,
    labels: ProviderConfigurationLabels = ProviderConfigurationLabels(),
    onDone: (() -> Unit)? = null,
) {
    val errors = if (showValidationErrors) {
        remember(state, apiKeyRequired) { state.validate(apiKeyRequired) }
    } else {
        emptyMap()
    }
    fun errorFor(field: ProviderConfigurationField): String? = errors[field]?.let(labels::messageFor)
    val next = KeyboardOptions(imeAction = ImeAction.Next)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
    ) {
        FormTextField(
            value = state.providerName,
            onValueChange = { onStateChange(state.copy(providerName = it)) },
            label = labels.providerName,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            errorText = errorFor(ProviderConfigurationField.PROVIDER_NAME),
            keyboardOptions = next,
        )
        FormTextField(
            value = state.baseUrl,
            onValueChange = { onStateChange(state.copy(baseUrl = it)) },
            label = labels.baseUrl,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            placeholder = "https://",
            errorText = errorFor(ProviderConfigurationField.BASE_URL),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Next,
            ),
        )
        FormTextField(
            value = state.model,
            onValueChange = { onStateChange(state.copy(model = it)) },
            label = labels.model,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            errorText = errorFor(ProviderConfigurationField.MODEL),
            keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, imeAction = ImeAction.Next),
        )
        SecretTextField(
            value = state.apiKey,
            onValueChange = { onStateChange(state.copy(apiKey = it)) },
            label = if (apiKeyRequired) labels.apiKey else labels.apiKeyOptional,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            errorText = errorFor(ProviderConfigurationField.API_KEY),
            imeAction = ImeAction.Done,
            // Keep the platform's default Done behavior (hide the keyboard) when no callback is set.
            keyboardActions = if (onDone != null) KeyboardActions(onDone = { onDone() }) else KeyboardActions.Default,
            showLabel = labels.showSecret,
            hideLabel = labels.hideSecret,
        )
    }
}
