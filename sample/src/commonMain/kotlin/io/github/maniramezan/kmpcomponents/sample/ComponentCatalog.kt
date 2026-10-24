package io.github.maniramezan.kmpcomponents.sample

import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.maniramezan.kmpcomponents.CheckStatus
import io.github.maniramezan.kmpcomponents.CheckStatusRow
import io.github.maniramezan.kmpcomponents.ConfirmDialog
import io.github.maniramezan.kmpcomponents.CountBadge
import io.github.maniramezan.kmpcomponents.DisclosureCard
import io.github.maniramezan.kmpcomponents.EmptyState
import io.github.maniramezan.kmpcomponents.HorizontalSplitPane
import io.github.maniramezan.kmpcomponents.InfoTooltip
import io.github.maniramezan.kmpcomponents.KmpTheme
import io.github.maniramezan.kmpcomponents.LocalReduceMotionEnabled
import io.github.maniramezan.kmpcomponents.MenuPicker
import io.github.maniramezan.kmpcomponents.ProviderConfigurationForm
import io.github.maniramezan.kmpcomponents.ProviderConfigurationLabels
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
import io.github.maniramezan.kmpcomponents.ThemeSingleChoiceSegmentedButtonRow
import io.github.maniramezan.kmpcomponents.ThemeSwitch
import io.github.maniramezan.kmpcomponents.TonalBadge
import io.github.maniramezan.kmpcomponents.isDark
import io.github.maniramezan.kmpcomponents.kmpSpacing
import io.github.maniramezan.kommon.designsystem.ColorToken
import io.github.maniramezan.kommon.designsystem.KommonDesignTokens
import io.github.maniramezan.kommon.designsystem.ShapeTokens
import io.github.maniramezan.kommon.designsystem.ThemeTokens

/**
 * A sectioned catalog shared by the desktop, Android, and iOS sample apps.
 * [onDarkThemeChanged] lets the host align native system bars with the resolved catalog appearance.
 */
@Composable
fun ComponentCatalog(onDarkThemeChanged: (Boolean) -> Unit = {}) {
    var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
    var brand by remember { mutableStateOf(Brand.DEFAULT) }
    var fontScale by remember { mutableStateOf(FontScale.DEFAULT) }
    var reduceMotion by remember { mutableStateOf(false) }
    var controlsExpanded by remember { mutableStateOf(false) }
    var rightToLeft by remember { mutableStateOf(false) }
    var section by remember { mutableStateOf(CatalogSection.FORMS) }
    val darkTheme = themeMode.isDark(isSystemInDarkTheme())
    SideEffect { onDarkThemeChanged(darkTheme) }
    val scrollState = rememberScrollState()
    LaunchedEffect(section) { scrollState.scrollTo(0) }
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, density.fontScale * fontScale.factor),
        LocalLayoutDirection provides if (rightToLeft) LayoutDirection.Rtl else LayoutDirection.Ltr,
        LocalReduceMotionEnabled provides if (reduceMotion) true else null,
    ) {
        KmpTheme(mode = themeMode, tokens = brand.tokens) {
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .safeDrawingPadding()
                        .verticalScroll(scrollState)
                        .padding(MaterialTheme.kmpSpacing.large),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.medium),
                ) {
                    MenuPicker(
                        items = CatalogSection.entries,
                        selected = section,
                        onSelect = { section = it },
                        label = "Catalog section",
                        itemLabel = CatalogSection::label,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    DisclosureCard(
                        expanded = controlsExpanded,
                        onExpandedChange = { controlsExpanded = it },
                        summary = { Text("Appearance and accessibility") },
                        detail = {
                            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small)) {
                                AppearanceSection(
                                    themeMode = themeMode,
                                    onThemeModeChange = { themeMode = it },
                                    brand = brand,
                                    onBrandChange = { brand = it },
                                    fontScale = fontScale,
                                    onFontScaleChange = { fontScale = it },
                                )
                                ThemeSwitch(
                                    checked = reduceMotion,
                                    onCheckedChange = { reduceMotion = it },
                                    label = "Reduce decorative motion",
                                )
                                ThemeSwitch(
                                    checked = rightToLeft,
                                    onCheckedChange = { rightToLeft = it },
                                    label = "Right-to-left layout",
                                )
                            }
                        },
                    )
                    Text("Examples use temporary local state. Choose another section to reset them.")
                    CatalogExamples(section)
                }
            }
        }
    }
}

internal enum class CatalogSection(val label: String) {
    FORMS("Forms"),
    ESSENTIALS("Lists, pickers, and feedback"),
    ACTIONS("Buttons and segments"),
    CHOICES("Radio, slider, and selection"),
    CONTAINERS("Containers and overlays"),
    NAVIGATION_BARS("App bars, paging, and feeds"),
    ASSISTANT("Chat and assistant cards"),
    DESKTOP("Copy, zoom, and status bar"),
    IMAGES("Image loading"),
    STATUS("Messages and badges"),
    NAVIGATION("Sidebar and tabs"),
    SELECTION("Selectable cards"),
    SPLIT_PANE("Horizontal split pane"),
    DIALOG("Confirmation dialog"),
}

@Composable
internal fun CatalogExamples(section: CatalogSection) {
    when (section) {
        CatalogSection.FORMS -> ProviderSection()
        CatalogSection.ESSENTIALS -> EssentialsSection()
        CatalogSection.ACTIONS -> ActionsSection()
        CatalogSection.CHOICES -> ChoicesSection()
        CatalogSection.CONTAINERS -> ContainersSection()
        CatalogSection.NAVIGATION_BARS -> NavigationBarsSection()
        CatalogSection.ASSISTANT -> AssistantSection()
        CatalogSection.DESKTOP -> DesktopSection()
        CatalogSection.IMAGES -> ImageSection()
        CatalogSection.STATUS -> StatusSection()
        CatalogSection.NAVIGATION -> NavigationSection()
        CatalogSection.SELECTION -> SelectionSection()
        CatalogSection.SPLIT_PANE -> SplitPaneSection()
        CatalogSection.DIALOG -> DialogSection()
    }
}

/** A rebranded token set next to the default, to show every Material role following the tokens. */
private enum class Brand(val label: String, val tokens: ThemeTokens) {
    DEFAULT("Default", KommonDesignTokens.default),
    GREEN(
        "Green theme",
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

@Composable
private fun <T> Segmented(options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    ThemeSingleChoiceSegmentedButtonRow(
        options = options.map(label),
        selectedIndex = options.indexOf(selected),
        onOptionSelected = { onSelect(options[it]) },
    )
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
    var pickerEnabled by remember { mutableStateOf(true) }
    SectionCard(
        title = "Appearance",
        description = "Preview light and dark modes, an alternate theme, and different text sizes.",
    ) {
        ThemeModeSelector(selected = themeMode, onSelect = onThemeModeChange)
        ThemeModeDropdown(
            selected = themeMode,
            onSelect = onThemeModeChange,
            label = "Color scheme",
            enabled = pickerEnabled,
            modifier = Modifier.fillMaxWidth(),
        )
        PickerAvailabilityControls(enabled = pickerEnabled, onEnabledChange = { pickerEnabled = it })
        Segmented(Brand.entries, brand, Brand::label, onBrandChange)
        Segmented(FontScale.entries, fontScale, FontScale::label, onFontScaleChange)
    }
}

@Composable
private fun ProviderSection() {
    var provider by remember { mutableStateOf(ProviderConfigurationState()) }
    var showValidation by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf("Idle") }
    var token by remember { mutableStateOf("") }
    SectionCard(
        title = "Forms and validation",
        description = "Editable example fields with local validation and selectable feedback states.",
        action = {
            TextButton(onClick = {
                provider = ProviderConfigurationState()
                showValidation = false
                feedback = "Idle"
            }) { Text("Reset") }
        },
    ) {
        ProviderConfigurationForm(
            state = provider,
            onStateChange = { provider = it },
            showValidationErrors = showValidation,
            labels = ProviderConfigurationLabels(
                providerName = "Connection name",
                baseUrl = "Service URL",
                model = "Resource name",
                apiKey = "Access token",
                baseUrlPlaceholder = "https://example.com",
            ),
            onDone = { showValidation = true },
        )
        ThemeSwitch(
            checked = showValidation,
            onCheckedChange = { showValidation = it },
            label = "Show validation errors",
        )
        MenuPicker(
            items = listOf("Idle", "Checking", "Success", "Failure"),
            selected = feedback,
            onSelect = { feedback = it },
            label = "Example status",
            itemLabel = { it },
        )
        CheckStatusRow(
            when (feedback) {
                "Checking" -> CheckStatus.Checking
                "Success" -> CheckStatus.Success("Example check succeeded")
                "Failure" -> CheckStatus.Failure("Example check failed")
                else -> CheckStatus.Idle
            },
        )
        val labelTopPadding = with(LocalDensity.current) { MaterialTheme.typography.bodySmall.lineHeight.toDp() / 2 }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
        ) {
            SecretTextField(
                value = token,
                onValueChange = { token = it },
                label = "Secret with icon toggle",
                modifier = Modifier.weight(1f),
                revealToggle = SecretRevealToggle.ICON,
            )
            InfoTooltip(
                "Use the eye button to show or hide the secret.",
                modifier = Modifier.padding(top = labelTopPadding),
                displayDurationMillis = null,
            )
        }
    }
}

@Composable
private fun StatusSection() {
    SectionCard(title = "Status messages and badges") {
        StatusMessage("An update is available.", Modifier.fillMaxWidth(), tone = StatusTone.INFO, title = "Info")
        StatusMessage("Changes saved.", Modifier.fillMaxWidth(), tone = StatusTone.SUCCESS, title = "Success")
        StatusMessage("Review your changes.", Modifier.fillMaxWidth(), tone = StatusTone.WARNING, title = "Warning")
        StatusMessage("Please try again.", Modifier.fillMaxWidth(), tone = StatusTone.ERROR, title = "Error")
        StatusMessage("A short message.")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kmpSpacing.small),
        ) {
            TonalBadge("Info")
            TonalBadge("Remove", color = MaterialTheme.colorScheme.error)
            TonalBadge("Ready", tone = StatusTone.SUCCESS)
            TonalBadge("Review", tone = StatusTone.WARNING)
            CountBadge(3, contentDescription = "3 items")
            CountBadge(250)
        }
    }
}

@Composable
private fun NavigationSection() {
    var section by remember { mutableStateOf("General") }
    val tabs = remember { mutableStateListOf("First", "Second", "Third") }
    var selectedTab by remember { mutableStateOf("First") }
    SectionCard(title = "Navigation", description = "Choose a destination, switch tabs, or close a tab.") {
        Row(Modifier.fillMaxWidth().height(SIDEBAR_DEMO_HEIGHT)) {
            SidebarNavigation(
                modifier = Modifier.width(SIDEBAR_DEMO_WIDTH),
                header = { Text("Settings", style = MaterialTheme.typography.titleMedium) },
            ) {
                listOf("General", "Appearance", "Advanced").forEach { name ->
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
    var selected by remember { mutableStateOf("First option") }
    SectionCard(title = "Selectable cards") {
        listOf(
            "First option" to "Example description",
            "Second option" to "Another description",
        ).forEach { (name, detail) ->
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
    SectionCard(
        title = "Split pane",
        description = "Drag the divider or use Left/Right when focused. Try the right-to-left layout switch above.",
    ) {
        Text("Leading pane: ${(split * 100).toInt()}%")
        TextButton(onClick = { split = 0.5f }) { Text("Reset split") }
        HorizontalSplitPane(
            split = split,
            onSplitChange = { split = it },
            modifier = Modifier.fillMaxWidth().height(SPLIT_DEMO_HEIGHT),
            leading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Leading pane") } },
            trailing = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Trailing pane") } },
        )
    }
}

private val SPLIT_DEMO_HEIGHT = 160.dp

@Composable
private fun DialogSection() {
    var showDialog by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    SectionCard(title = "Confirm dialog") {
        OutlinedButton(onClick = { showDialog = true }) { Text("Delete example item…") }
        result?.let { Text(it) }
    }
    if (showDialog) {
        ConfirmDialog(
            title = "Delete example item?",
            message = "Confirm or cancel to see the callback result.",
            onConfirm = {
                showDialog = false
                result = "Deleted"
            },
            onDismiss = {
                showDialog = false
                result = "Cancelled"
            },
            confirmLabel = "Delete",
            destructive = true,
        )
    }
}
