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

private val HTTP_SCHEME = Regex("^https?://", RegexOption.IGNORE_CASE)
private val HOST_LABEL = Regex("^[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?$")
private val IPV6_GROUP = Regex("^[0-9A-Fa-f]{1,4}$")

private fun isIpv4Host(host: String): Boolean {
    val octets = host.split('.')
    return octets.size == 4 && octets.all { octet ->
        octet.isNotEmpty() && octet.all(Char::isDigit) && octet.toIntOrNull()?.let { it in 0..255 } == true
    }
}

private fun isIpv6Host(host: String): Boolean {
    val compressionAt = host.indexOf("::")
    if (compressionAt >= 0 && host.indexOf("::", compressionAt + 2) >= 0) return false
    val parts = if (compressionAt >= 0) {
        listOf(host.substring(0, compressionAt), host.substring(compressionAt + 2))
    } else {
        listOf(host)
    }
    if (parts.any { it.isNotEmpty() && (it.startsWith(':') || it.endsWith(':')) }) return false
    val groups = parts.flatMap { if (it.isEmpty()) emptyList() else it.split(':') }
    var groupCount = 0
    groups.forEachIndexed { index, group ->
        if ('.' in group) {
            if (index != groups.lastIndex || !isIpv4Host(group)) return false
            groupCount += 2
        } else {
            if (!IPV6_GROUP.matches(group)) return false
            groupCount++
        }
    }
    return if (compressionAt >= 0) groupCount < 8 else groupCount == 8
}

private fun isHttpUrl(value: String): Boolean {
    val url = value.trim()
    if (url.any { it.isWhitespace() || it.code < 0x20 }) return false
    val scheme = HTTP_SCHEME.find(url)?.value ?: return false
    val authority = url.substring(scheme.length).takeWhile { it != '/' && it != '?' && it != '#' }
    if (authority.isEmpty() || '@' in authority) return false

    val portSuffix = if (authority.startsWith('[')) {
        val closingBracket = authority.indexOf(']')
        if (closingBracket < 2) return false
        val host = authority.substring(1, closingBracket)
        if (!isIpv6Host(host)) return false
        authority.substring(closingBracket + 1)
    } else {
        val host = authority.substringBefore(':')
        if (host.split('.').any { !HOST_LABEL.matches(it) }) return false
        authority.substring(host.length)
    }
    if (portSuffix.isEmpty()) return true
    if (!portSuffix.startsWith(':')) return false
    val port = portSuffix.substring(1)
    return port.isNotEmpty() && port.all(Char::isDigit) && port.toIntOrNull()?.let { it in 1..65535 } == true
}

/**
 * Validates every field and returns the first problem found per field; an empty map means valid.
 *
 * All fields are required except [ProviderConfigurationState.apiKey] when [apiKeyRequired] is
 * false. [ProviderConfigurationState.baseUrl] must be an absolute `http` or `https` URL with a
 * valid host and, if present, a port from 1 to 65535. Surrounding whitespace is ignored.
 */
public fun ProviderConfigurationState.validate(
    apiKeyRequired: Boolean = true,
): Map<ProviderConfigurationField, ProviderConfigurationError> = buildMap {
    if (providerName.isBlank()) put(ProviderConfigurationField.PROVIDER_NAME, ProviderConfigurationError.REQUIRED)
    when {
        baseUrl.isBlank() -> put(ProviderConfigurationField.BASE_URL, ProviderConfigurationError.REQUIRED)
        !isHttpUrl(baseUrl) ->
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
    public val baseUrlPlaceholder: String = "https://",
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
            placeholder = labels.baseUrlPlaceholder,
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
