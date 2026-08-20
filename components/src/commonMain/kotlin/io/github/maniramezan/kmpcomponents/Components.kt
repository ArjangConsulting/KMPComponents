package io.github.maniramezan.kmpcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation

@Composable
public fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.kmpSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            description?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            content()
        }
    }
}

@Immutable
public data class ProviderConfigurationState(
    public val providerName: String = "",
    public val baseUrl: String = "",
    public val model: String = "",
    public val apiKey: String = "",
)

@Composable
public fun ProviderConfigurationForm(
    state: ProviderConfigurationState,
    onStateChange: (ProviderConfigurationState) -> Unit,
    modifier: Modifier = Modifier,
    apiKeyRequired: Boolean = true,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
    ) {
        OutlinedTextField(
            value = state.providerName,
            onValueChange = { onStateChange(state.copy(providerName = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Provider name") },
            singleLine = true,
        )
        OutlinedTextField(
            value = state.baseUrl,
            onValueChange = { onStateChange(state.copy(baseUrl = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Base URL") },
            singleLine = true,
        )
        OutlinedTextField(
            value = state.model,
            onValueChange = { onStateChange(state.copy(model = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Model") },
            singleLine = true,
        )
        OutlinedTextField(
            value = state.apiKey,
            onValueChange = { onStateChange(state.copy(apiKey = it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(if (apiKeyRequired) "API key" else "API key (optional)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
        )
    }
}
