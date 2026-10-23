package io.github.maniramezan.kmpcomponents.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.maniramezan.kmpcomponents.CheckStatus
import io.github.maniramezan.kmpcomponents.CheckStatusRow
import io.github.maniramezan.kmpcomponents.ConfirmDialog
import io.github.maniramezan.kmpcomponents.CountBadge
import io.github.maniramezan.kmpcomponents.EmptyState
import io.github.maniramezan.kmpcomponents.HorizontalSplitPane
import io.github.maniramezan.kmpcomponents.InfoTooltip
import io.github.maniramezan.kmpcomponents.KmpTheme
import io.github.maniramezan.kmpcomponents.ProviderConfigurationForm
import io.github.maniramezan.kmpcomponents.ProviderConfigurationState
import io.github.maniramezan.kmpcomponents.SecretRevealToggle
import io.github.maniramezan.kmpcomponents.SecretTextField
import io.github.maniramezan.kmpcomponents.SectionCard
import io.github.maniramezan.kmpcomponents.SelectableCard
import io.github.maniramezan.kmpcomponents.SidebarItem
import io.github.maniramezan.kmpcomponents.SidebarNavigation
import io.github.maniramezan.kmpcomponents.StatusMessage
import io.github.maniramezan.kmpcomponents.StatusTone
import io.github.maniramezan.kmpcomponents.TabChip
import io.github.maniramezan.kmpcomponents.TabChipRow
import io.github.maniramezan.kmpcomponents.ThemeMode
import io.github.maniramezan.kmpcomponents.ThemeModeDropdown
import io.github.maniramezan.kmpcomponents.ThemeModeSelector
import io.github.maniramezan.kmpcomponents.TonalBadge
import io.github.maniramezan.kmpcomponents.isChecking
import io.github.maniramezan.kmpcomponents.isValid
import io.github.maniramezan.kmpcomponents.kmpSpacing
import io.github.maniramezan.kmpcomponents.kmpTypography
import io.github.maniramezan.kommon.designsystem.ColorToken
import io.github.maniramezan.kommon.designsystem.KommonDesignTokens
import io.github.maniramezan.kommon.designsystem.ShapeTokens
import io.github.maniramezan.kommon.designsystem.ThemeTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Every public component in one scrollable screen. Add new components here when you add them. */
@Composable
fun ComponentCatalog() {
    var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
    var brand by remember { mutableStateOf(Brand.DEFAULT) }
    var fontScale by remember { mutableStateOf(FontScale.DEFAULT) }
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, density.fontScale * fontScale.factor)) {
        KmpTheme(mode = themeMode, tokens = brand.tokens) {
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .safeDrawingPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(MaterialTheme.kmpSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.medium),
                ) {
                    AppearanceSection(
                        themeMode = themeMode,
                        onThemeModeChange = { themeMode = it },
                        brand = brand,
                        onBrandChange = { brand = it },
                        fontScale = fontScale,
                        onFontScaleChange = { fontScale = it },
                    )
                    ProviderSection()
                    EssentialsSection()
                    ActionsSection()
                    ChoicesSection()
                    ContainersSection()
                    NavigationBarsSection()
                    AssistantSection()
                    StatusSection()
                    NavigationSection()
                    SelectionSection()
                    SplitPaneSection()
                    DialogSection()
                }
            }
        }
    }
}

/** A rebranded token set next to the default, to show every Material role following the tokens. */
private enum class Brand(val label: String, val tokens: ThemeTokens) {
    DEFAULT("kommon", KommonDesignTokens.default),
    GREEN(
        "Green brand",
        KommonDesignTokens.default.copy(
            lightColors = KommonDesignTokens.default.lightColors.copy(primary = ColorToken(0xFF_00_6C_4F)),
            darkColors = KommonDesignTokens.default.darkColors.copy(primary = ColorToken(0xFF_7F_D8_B5)),
            shapes = ShapeTokens(smallRadius = 2f, mediumRadius = 4f, largeRadius = 6f),
        ),
    ),
}

private enum class FontScale(val label: String, val factor: Float) {
    DEFAULT("100%", 1f),
    LARGE("130%", 1.3f),
    HUGE("200%", 2f),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> Segmented(options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    SingleChoiceSegmentedButtonRow {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                label = { Text(label(option)) },
            )
        }
    }
}

@Composable
private fun AppearanceSection(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    brand: Brand,
    onBrandChange: (Brand) -> Unit,
    fontScale: FontScale,
    onFontScaleChange: (FontScale) -> Unit,
) {
    SectionCard(
        title = "Appearance",
        description = "ThemeModeSelector and ThemeModeDropdown drive KmpTheme. Tokens and font scale are " +
            "catalog-only switches for checking rebranding and large text.",
    ) {
        ThemeModeSelector(selected = themeMode, onSelect = onThemeModeChange)
        ThemeModeDropdown(
            selected = themeMode,
            onSelect = onThemeModeChange,
            label = "Color scheme",
            modifier = Modifier.fillMaxWidth(),
        )
        Segmented(Brand.entries, brand, Brand::label, onBrandChange)
        Segmented(FontScale.entries, fontScale, FontScale::label, onFontScaleChange)
    }
}

@Composable
private fun ProviderSection() {
    var provider by remember { mutableStateOf(ProviderConfigurationState()) }
    var submitted by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf<ProviderConfigurationState?>(null) }
    var check by remember { mutableStateOf<CheckStatus>(CheckStatus.Idle) }
    val scope = rememberCoroutineScope()
    fun submit() {
        submitted = true
        if (provider.isValid()) saved = provider
    }

    SectionCard(
        title = "Provider",
        description = "ProviderConfigurationForm built from FormTextField and SecretTextField, with a " +
            "CheckStatusRow for the connection test.",
        action = {
            TextButton(onClick = {
                provider = ProviderConfigurationState()
                submitted = false
                check = CheckStatus.Idle
            }) { Text("Reset") }
        },
    ) {
        ProviderConfigurationForm(
            state = provider,
            onStateChange = {
                provider = it
                check = CheckStatus.Idle
            },
            showValidationErrors = submitted,
            onDone = ::submit,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
        ) {
            Button(onClick = ::submit) { Text("Save") }
            OutlinedButton(
                onClick = {
                    check = CheckStatus.Checking
                    scope.launch {
                        delay(FAKE_CHECK_MILLIS)
                        check = if (provider.isValid()) {
                            CheckStatus.Success("Connected to ${provider.model}")
                        } else {
                            CheckStatus.Failure("Fill in every field first")
                        }
                    }
                },
                enabled = !check.isChecking,
            ) { Text("Test connection") }
        }
        CheckStatusRow(check)
        saved?.let {
            // toString() redacts the API key, so this is safe to display or log.
            Text(it.toString(), style = MaterialTheme.kmpTypography.code)
        }
        var token by remember { mutableStateOf("") }
        Row(verticalAlignment = Alignment.CenterVertically) {
            SecretTextField(
                value = token,
                onValueChange = { token = it },
                label = "Access token (icon toggle)",
                modifier = Modifier.weight(1f),
                revealToggle = SecretRevealToggle.ICON,
            )
            InfoTooltip("SecretRevealToggle.ICON shows an eye instead of Show/Hide text.")
        }
    }
}

private const val FAKE_CHECK_MILLIS = 1_200L

@Composable
private fun StatusSection() {
    SectionCard(title = "Status messages and badges") {
        StatusMessage("Uses the primary color.", Modifier.fillMaxWidth(), tone = StatusTone.INFO, title = "Info")
        StatusMessage("Uses kmpColors.success.", Modifier.fillMaxWidth(), tone = StatusTone.SUCCESS, title = "Success")
        StatusMessage("Uses kmpColors.warning.", Modifier.fillMaxWidth(), tone = StatusTone.WARNING, title = "Warning")
        StatusMessage("Uses colorScheme.error.", Modifier.fillMaxWidth(), tone = StatusTone.ERROR, title = "Error")
        StatusMessage("Sizes to its content without a fillMaxWidth modifier.")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
        ) {
            TonalBadge("GET")
            TonalBadge("DELETE", color = MaterialTheme.colorScheme.error)
            TonalBadge("200", tone = StatusTone.SUCCESS)
            TonalBadge("404", tone = StatusTone.WARNING)
            CountBadge(3, contentDescription = "3 variants")
            CountBadge(250)
        }
    }
}

@Composable
private fun NavigationSection() {
    var section by remember { mutableStateOf("General") }
    val tabs = remember { mutableStateListOf("Success", "Not found", "Server error") }
    var selectedTab by remember { mutableStateOf("Success") }
    SectionCard(title = "Navigation", description = "SidebarNavigation, SidebarItem, TabChipRow, and EmptyState.") {
        Row(Modifier.fillMaxWidth().height(SIDEBAR_DEMO_HEIGHT)) {
            SidebarNavigation(
                modifier = Modifier.width(SIDEBAR_DEMO_WIDTH),
                header = { Text("Settings", style = MaterialTheme.typography.titleMedium) },
            ) {
                listOf("General", "AI", "Advanced").forEach { name ->
                    SidebarItem(label = name, selected = section == name, onClick = { section = name })
                }
            }
            EmptyState(
                title = "$section settings",
                modifier = Modifier.weight(1f).fillMaxSize(),
                message = "Nothing to configure in this demo.",
            )
        }
        TabChipRow {
            tabs.forEach { name ->
                TabChip(
                    label = name,
                    selected = selectedTab == name,
                    onClick = { selectedTab = name },
                    onClose = if (tabs.size > 1) {
                        {
                            tabs.remove(name)
                            if (selectedTab == name) selectedTab = tabs.first()
                        }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

private val SIDEBAR_DEMO_HEIGHT = 220.dp
private val SIDEBAR_DEMO_WIDTH = 160.dp

@Composable
private fun SelectionSection() {
    var selected by remember { mutableStateOf("Ollama") }
    SectionCard(title = "Selectable cards") {
        listOf("Ollama" to "llama3 · local", "OpenAI" to "gpt · API key in keychain").forEach { (name, detail) ->
            SelectableCard(selected = selected == name, onClick = {
                selected = name
            }, modifier = Modifier.fillMaxWidth()) {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text(detail, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SplitPaneSection() {
    var split by remember { mutableFloatStateOf(0.5f) }
    SectionCard(title = "Split pane", description = "Drag the divider, or focus it and use the arrow keys.") {
        HorizontalSplitPane(
            split = split,
            onSplitChange = { split = it },
            modifier = Modifier.fillMaxWidth().height(SPLIT_DEMO_HEIGHT),
            leading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Console") } },
            trailing = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Device") } },
        )
    }
}

private val SPLIT_DEMO_HEIGHT = 160.dp

@Composable
private fun DialogSection() {
    var showDialog by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    SectionCard(title = "Confirm dialog") {
        OutlinedButton(onClick = { showDialog = true }) { Text("Delete all variants…") }
        result?.let { Text(it) }
    }
    if (showDialog) {
        ConfirmDialog(
            title = "Delete all variants?",
            message = "This removes every response variant for the endpoint.",
            onConfirm = {
                showDialog = false
                result = "Deleted"
            },
            onDismiss = {
                showDialog = false
                result = "Cancelled"
            },
            confirmLabel = "Delete all",
            destructive = true,
        )
    }
}
