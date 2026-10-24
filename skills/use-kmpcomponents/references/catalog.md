# Component catalog for agents

Generated from the README catalog and public declaration locations. Maintainers regenerate
it in the library checkout with `python3 scripts/update_agent_catalog.py` after updating
those sources. Consuming applications do not need the generator.

This index describes the checkout that produced it. Source links point to `main` for
discovery; inspect a tag or source artifact matching the application's resolved dependency
version before copying a signature. A name in this index does not prove it exists in an
older release. Core imports use `io.github.maniramezan.kmpcomponents`; the image module
uses `io.github.maniramezan.kmpcomponents.image` and has the same targets.

## Theme and forms

| Components | Purpose | Source |
| --- | --- | --- |
| `KmpTheme` | Applies kommon `ThemeTokens` as a Material 3 theme for a `ThemeMode`. | [KmpTheme.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/KmpTheme.kt) |
| `ThemeModeSelector` | Segmented System / Light / Dark control. | [ThemeModeSelector.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/ThemeModeSelector.kt) |
| `ThemeModeDropdown` | Labeled dropdown for the same choice, for settings forms. Built on `MenuPicker`. | [ThemeModeSelector.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/ThemeModeSelector.kt) |
| `ThemeMode.toggled` | The opposite explicit mode, for a single light/dark toggle button. | [ThemeMode.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/ThemeMode.kt) |
| `SectionCard` | Titled group of content with optional description and header action. Title is an accessibility heading. | [SectionCard.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/SectionCard.kt) |
| `FormTextField` | Outlined text field with label, placeholder, and supporting/error text. | [FormTextField.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/FormTextField.kt) |
| `SecretTextField` | Masked field with a Show/Hide text or eye-icon toggle and a password keyboard without autocorrect. | [FormTextField.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/FormTextField.kt) |
| `ProviderConfigurationForm` | Provider name, base URL, model, and API key fields with validation, built from the fields above. | [ProviderConfiguration.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/ProviderConfiguration.kt) |
| `CheckStatus` / `CheckStatusRow` | State and one-line display of an on-demand check such as "Test connection" (idle, spinner, success, failure). Results are announced. | [CheckStatus.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/CheckStatus.kt) |
| `MenuPicker` | Labeled dropdown field for picking one item of any type, for forms where a segmented control does not fit. | [MenuPicker.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/MenuPicker.kt) |
| `SearchField` | Single-line search field with a search glyph, a clear button, and a keyboard Search action. A blank placeholder draws none. | [SearchField.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/SearchField.kt) |
| `ThemeSwitch` | Setting row with a label, optional supporting text and tooltip, and a trailing switch. The whole row toggles. | [ToggleRows.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/ToggleRows.kt) |
| `ThemeCheckbox` / `ThemeTriStateCheckbox` | Checkbox rows with a label; the tri-state one is for "select all" over a group. | [ToggleRows.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/ToggleRows.kt) |
| `RadioGroup` | Vertical radio list; each full-width row is the tap target. | [InputControls.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/InputControls.kt) |
| `ThemeSlider` | Full-width slider with an optional spoken label, value description, and steps. | [InputControls.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/InputControls.kt) |
| `SelectionList` / `SelectionListContent` / `SelectionListNode` | Searchable one- or two-level picker, single or multiple choice, in a bottom sheet or inline. Parents expand inline, scroll into view, and summarize their selected children when collapsed. | [SelectionList.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/SelectionList.kt), [SelectionListNode.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/SelectionListNode.kt) |

## Feedback

| Components | Purpose | Source |
| --- | --- | --- |
| `StatusMessage` | Inline info / success / warning / error message. Warnings and errors are announced by screen readers. Sizes to its content. | [StatusMessage.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/StatusMessage.kt) |
| `TonalBadge` | Short label on a tinted background (HTTP method, status code, severity), by color or `StatusTone`. | [Badges.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Badges.kt) |
| `CountBadge` | Pill with a count, capped as `99+`, with an optional spoken description. | [Badges.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Badges.kt) |
| `EmptyState` | Centered title, message, and optional action for an empty pane. | [EmptyState.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/EmptyState.kt) |
| `ConfirmDialog` | Confirm / cancel dialog, optionally with a destructive (error-colored) confirm button. | [ConfirmDialog.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/ConfirmDialog.kt) |
| `InfoTooltip` | Info glyph showing an explanation on hover, long-press, or focus; configurable hover duration (`displayDurationMillis = null` keeps it visible while hovered); screen readers read it directly. | [InfoTooltip.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/InfoTooltip.kt) |
| `NoticeCard` | Full-width `StatusTone` notice with an optional action ("Retry") and dismiss button. | [NoticeCard.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/NoticeCard.kt) |
| `ProgressIndicator` | Centered spinner or determinate bar with an announced label, for a loading pane. | [ProgressIndicator.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/ProgressIndicator.kt) |
| `LocalReduceMotionEnabled` | Optional override for decorative animations and page hints; null follows live system settings. | [ReduceMotion.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/ReduceMotion.kt) |
| `rememberReduceMotionEnabled` | Returns the effective reduced-motion preference (the override if set, else the system), so app animations can follow it. | [ReduceMotion.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/ReduceMotion.kt) |
| `InlineProgressIndicator` | Text-sized spinner and label for work in progress inside a row or toolbar. | [ProgressIndicator.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/ProgressIndicator.kt) |

## Actions

| Components | Purpose | Source |
| --- | --- | --- |
| `PrimaryButton` / `SecondaryButton` | Filled and outlined text buttons for the primary and secondary action. | [Buttons.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Buttons.kt) |
| `ThemeTextButton` | Low-emphasis text button, optionally `destructive` (error-colored). | [Buttons.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Buttons.kt) |
| `ThemeIconButton` | Icon-only button; its description is also shown as a tooltip. | [Buttons.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Buttons.kt) |
| `LoadingIconButton` | Icon button that shows a spinner and ignores clicks while loading. | [Buttons.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Buttons.kt) |
| `FAB` / `ThemeExtendedFloatingActionButton` | Floating action buttons; the extended one is named by its visible label. | [Buttons.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Buttons.kt) |
| `SegmentedControl` | Pill-shaped segments for one choice, with `SegmentDensity` and `SegmentWidthMode`. | [SegmentedControl.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/SegmentedControl.kt) |
| `ThemeSingleChoiceSegmentedButtonRow` | Material's connected, outlined segmented buttons with a check mark; scrolls to preserve full labels in tight layouts. | [SegmentedControl.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/SegmentedControl.kt) |
| `SegmentedContent` | Segment bar (pill, underline, or bare) above content that crossfades; scrolls with edge fades instead of truncating titles. | [SegmentedContent.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/SegmentedContent.kt) |
| `PillChip` | Capsule chip with selected and unselected looks; selectable when clickable, static otherwise. | [PillChip.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/PillChip.kt) |
| `ActionPill` | Capsule command with arbitrary content and no selected state. | [ActionPill.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/ActionPill.kt) |

## Containers, feedback, and layout

| Components | Purpose | Source |
| --- | --- | --- |
| `ThemeCard` / `ThemeSurface` | Elevated (optionally clickable) and flat full-width containers that stack their content. | [Containers.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Containers.kt) |
| `DisclosureCard` | Card with an always-visible summary that reveals detail; announces expanded or collapsed. | [Layout.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Layout.kt) |
| `OverlayCard` | Translucent rounded container for labels over images or video. | [Layout.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Layout.kt) |
| `ThemeDialog` | Alert dialog with a confirm action and an optional dismiss action; `ConfirmDialog` is built on it. | [Containers.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Containers.kt) |
| `BottomSheet` | Modal bottom sheet that always opens fully expanded. | [Containers.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Containers.kt) |
| `Toast` / `ToastHost` / `ToastHostState` | Transient messages with optional icon and action, shown one at a time like snackbars. `showToast` suspends and returns a `ToastResult`. | [Toast.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Toast.kt) |
| `ThemeSnackbar` | Snackbar with an optional action, announced when shown standalone. | [Containers.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Containers.kt) |
| `LoadingState` / `ErrorState` | Pane placeholders for loading and failure, announced by screen readers; siblings of `EmptyState`. | [StateViews.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/StateViews.kt) |
| `Skeleton` / `SkeletonBlock` / `Modifier.skeletonShimmer` | Decorative loading placeholders and a sweep that stays still when the system reduces motion. | [Skeleton.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Skeleton.kt) |
| `ThemeHorizontalDivider` / `ThemeVerticalDivider` | Rules in the outline variant color. | [Layout.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Layout.kt) |
| `FlowLayout` | Wrapping row with the standard gaps, for chips and button groups. | [Layout.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Layout.kt) |
| `AdaptiveContentContainer` | Centers content and caps its width on wide windows. | [Layout.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Layout.kt) |

## Chat and assistant

| Components | Purpose | Source |
| --- | --- | --- |
| `ChatLog` / `ChatMessage` / `ChatMessageState` | Conversation with user and assistant bubbles, typing and retryable error states; follows new messages only while the reader is at the bottom. | [ChatLog.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/ChatLog.kt) |
| `TypingIndicator` | Three-dot "reply is being written" wave; still when the system reduces motion. | [TypingIndicator.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/TypingIndicator.kt) |
| `rememberTypewriterReveal` | Reveals streamed text a character at a time, continuing as it grows; shows it at once when the system reduces motion. | [TypewriterReveal.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/TypewriterReveal.kt) |
| `LoadingOverlay` | Scrim that blocks interaction with its content during work the user must wait for, and announces it. | [LoadingOverlay.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/LoadingOverlay.kt) |
| `AssistantContextCard` | Title, highlighted value, optional quote, and footnote describing what the assistant sees. | [AssistantComponents.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/AssistantComponents.kt) |
| `AssistantQuickActionChips` | Scrolling filter chips for quick actions, with caller-owned state. | [AssistantComponents.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/AssistantComponents.kt) |
| `AssistantStatusBanner` | Announced error banner for an unavailable provider or blocking failure. | [AssistantComponents.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/AssistantComponents.kt) |
| `AssistantLimitPromptCard` | Quota or upgrade prompt with primary and secondary actions. | [AssistantComponents.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/AssistantComponents.kt) |
| `AssistantDisclaimerFooter` | Low-emphasis disclaimer line. | [AssistantComponents.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/AssistantComponents.kt) |

## Images (`components-image`)

| Components | Purpose | Source |
| --- | --- | --- |
| `CachedAsyncImage` / `ImageCachePolicy` | Loads a URL, file, or bytes with explicit memory, disk, and network cache policies, plus placeholder, error, and fallback painters. Package `io.github.maniramezan.kmpcomponents.image`. | [CachedAsyncImage.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components-image/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/image/CachedAsyncImage.kt) |

## Navigation and layout

| Components | Purpose | Source |
| --- | --- | --- |
| `SidebarNavigation` / `SidebarItem` | Desktop-style side navigation with a header and selectable destinations. | [SidebarNavigation.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/SidebarNavigation.kt) |
| `TabChipRow` / `TabChip` | Scrolling row of compact tabs with optional close controls. | [TabChip.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/TabChip.kt) |
| `SelectableCard` | Outlined card with a selection box, checked state, and visible keyboard focus; reports selection to accessibility services. | [SelectableCard.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/SelectableCard.kt) |
| `HorizontalSplitPane` | Two panes with a divider you can drag, move with arrow keys, or adjust with a screen reader. | [SplitPane.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/SplitPane.kt) |
| `VerticalSplitPane` | The same divider between a top and a bottom pane, moved with the Up/Down keys. | [VerticalSplitPane.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/VerticalSplitPane.kt) |
| `StatusBar` / `StatusBarItem` / `StatusBarSeparator` | Desktop status bar; items share space and ellipsize rather than dropping off. | [StatusBar.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/StatusBar.kt) |
| `ZoomableImage` | Fitted image with wheel, keyboard, and drag zoom/pan and an optional marker ring; announces the zoom level. | [ZoomableImage.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/ZoomableImage.kt) |
| `CopyButton` | Copies text to the clipboard, then shows a check and announces "Copied". | [CopyButton.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/CopyButton.kt) |
| `Modifier.focusNextOnTab` | Makes Tab and Shift+Tab move focus out of fields that would otherwise consume the key. | [FocusTraversal.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/FocusTraversal.kt) |
| `SectionHeader` | Title (an accessibility heading), optional subtitle, and trailing actions above a list or pane. | [SectionHeader.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/SectionHeader.kt) |
| `ThemeListItem` / `LazyList` | Material-style list row read as one item, and a padded full-width `LazyColumn`. | [Lists.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Lists.kt) |
| `LoadMoreFooter` | Last item of an infinite list; asks for the next page when it scrolls into view and announces loading. | [Lists.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Lists.kt) |
| `ThemeTopAppBar` / `ThemeMediumTopAppBar` / `ThemeLargeTopAppBar` | Top app bars on the surface color with primary-tinted icons. | [Navigation.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Navigation.kt) |
| `ThemeTabRow` | Material primary tab row for peers at one level. | [Navigation.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Navigation.kt) |
| `TabBar` / `TabBarItem` / `TabBarScrollBehavior` | Bottom navigation with badges, a selected pill, and optional hide-on-scroll. | [Navigation.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Navigation.kt), [TabBarScrollBehavior.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/TabBarScrollBehavior.kt) |
| `NavRail` | Side rail sharing `TabBarItemData` and colors with `TabBar`. | [Navigation.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Navigation.kt) |
| `AdaptiveNavScaffold` | `TabBar` in compact windows, `NavRail` from 600 dp up. | [Navigation.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Navigation.kt) |
| `PaginatedContent` | Swipeable pages with a synced title row, dots or progress footer, and an optional forward-only mode. | [PaginatedContent.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/PaginatedContent.kt) |
| `PageIndicator` | Dots for the current page of a pager or carousel; the current dot is a wider pill, and an optional description is announced. | [PageIndicator.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/PageIndicator.kt) |
| `ShowcaseRow` / `ShowcaseFeed` | Peeking horizontal carousels and an app-store style feed of them. | [Showcase.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Showcase.kt) |
| `FlipCard` | Two-sided card that turns in 3D; only the visible face is read by screen readers. | [FlipCard.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/FlipCard.kt) |
| `AppText` / `AppTextStyle` | Text in a semantic role, including heavier weight variants of Material slots. | [AppText.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/AppText.kt) |
| `ContentRow` | List row with title, secondary and supporting text, leading/trailing slots, and optional click and selection. | [ContentRow.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/ContentRow.kt) |
| `KeyValueRow` | Read-only label/value pair for details panels; flows onto separate lines when space is tight and is read as one item. | [KeyValueRow.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/KeyValueRow.kt) |
| `Avatar` / `AvatarSize` | Circular image or initials for a person, read as one named image; sizes follow the text size. | [Avatar.kt](https://github.com/ArjangConsulting/KMPComponents/blob/main/components/src/commonMain/kotlin/io/github/maniramezan/kmpcomponents/Avatar.kt) |
