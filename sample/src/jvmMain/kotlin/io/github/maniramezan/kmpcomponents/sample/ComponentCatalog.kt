package io.github.maniramezan.kmpcomponents.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.maniramezan.kmpcomponents.KmpTheme
import io.github.maniramezan.kmpcomponents.ProviderConfigurationForm
import io.github.maniramezan.kmpcomponents.ProviderConfigurationState
import io.github.maniramezan.kmpcomponents.SectionCard
import io.github.maniramezan.kmpcomponents.StatusMessage
import io.github.maniramezan.kmpcomponents.StatusTone
import io.github.maniramezan.kmpcomponents.ThemeMode
import io.github.maniramezan.kmpcomponents.ThemeModeSelector
import io.github.maniramezan.kmpcomponents.isValid
import io.github.maniramezan.kmpcomponents.kmpSpacing
import io.github.maniramezan.kmpcomponents.kmpTypography

/** Every public component in one scrollable screen. Add new components here when you add them. */
@Composable
fun ComponentCatalog() {
    var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
    KmpTheme(mode = themeMode) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(MaterialTheme.kmpSpacing.large),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.medium),
            ) {
                SectionCard(title = "Appearance", description = "ThemeModeSelector drives KmpTheme.") {
                    ThemeModeSelector(selected = themeMode, onSelect = { themeMode = it })
                }
                ProviderSection()
                StatusSection()
            }
        }
    }
}

@Composable
private fun ProviderSection() {
    var provider by remember { mutableStateOf(ProviderConfigurationState()) }
    var submitted by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf<ProviderConfigurationState?>(null) }
    fun submit() {
        submitted = true
        if (provider.isValid()) saved = provider
    }

    SectionCard(
        title = "Provider",
        description = "ProviderConfigurationForm built from FormTextField and SecretTextField.",
        action = {
            TextButton(onClick = {
                provider = ProviderConfigurationState()
                submitted = false
            }) { Text("Reset") }
        },
    ) {
        ProviderConfigurationForm(
            state = provider,
            onStateChange = { provider = it },
            showValidationErrors = submitted,
            onDone = ::submit,
        )
        Button(onClick = ::submit, modifier = Modifier.fillMaxWidth()) { Text("Save") }
        saved?.let {
            // toString() redacts the API key, so this is safe to display or log.
            Text(it.toString(), style = MaterialTheme.kmpTypography.code)
        }
    }
}

@Composable
private fun StatusSection() {
    SectionCard(title = "Status messages") {
        StatusMessage("Uses the primary color.", tone = StatusTone.INFO, title = "Info")
        StatusMessage("Uses kmpColors.success.", tone = StatusTone.SUCCESS, title = "Success")
        StatusMessage("Uses kmpColors.warning.", tone = StatusTone.WARNING, title = "Warning")
        StatusMessage("Uses colorScheme.error.", tone = StatusTone.ERROR, title = "Error")
    }
}
