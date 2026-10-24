# Shared-component accessibility audit

This audit covers the common Compose implementation and desktop-rendered behavior of the component
catalog. It uses the repository's design style guide and the WCAG 2.2 criteria for
[use of color](https://www.w3.org/WAI/WCAG22/Understanding/use-of-color.html),
[text contrast](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html),
[non-text contrast](https://www.w3.org/WAI/WCAG22/Understanding/non-text-contrast.html),
[visible focus](https://www.w3.org/WAI/WCAG22/Understanding/focus-visible.html), and
[names, roles, and values](https://www.w3.org/WAI/WCAG22/Understanding/name-role-value.html).
The repository requires 48 dp interactive targets, 4.5:1 normal text contrast, and 3:1 enabled
control boundaries. These are the acceptance thresholds used by the tests.

## Confirmed findings and remediation

| Priority | Finding | Resolution and evidence |
| --- | --- | --- |
| P1 | Selected cards relied on a subtle tint, and unselected cards looked like static containers. | `SelectableCard` has an outlined selection box, a check when selected, a card boundary, and explicit foreground roles. `AccessibilityAuditTest` verifies that selection still changes the rendered graphic when the two background colors and borders are identical. |
| P1 | Custom cards, sidebar destinations, tab chips, content rows, and labeled toggles lacked an explicit keyboard focus outline. Secondary header, notice, and secret-reveal actions also bypassed shared focus styling. | These controls use the common focus outline or shared buttons. `AccessibilityRegressionTest` checks the rendered border for 27 control cases in default/alternate themes, light/dark modes, and 100%/200% text sizes. |
| P1 | Small success-accent text did not reach 4.5:1 contrast on its tinted container. Inline status accents could also lose contrast on card surfaces. | `TokenAdapters` chooses an existing on-surface foreground when the preferred accent is unreadable. Badges, status/notice titles, and inline check results share that mapping. Common tests cover foreground/background pairs across both themes and modes. |
| P2 | Tab-close glyphs were directly clickable at glyph dimensions; sidebar items and short tabs could be below the repository's minimum target size. | Close actions use the shared tooltip icon button; tabs, sidebar items, and selectable cards reserve minimum targets. Bounds are checked at both font scales in LTR and RTL. Closing a tab and selecting it remain independent actions. |
| P2 | Sidebar and content-row selection depended on background color. Pill segments also relied on their fill. | Selected sidebar/content rows include a decorative check; selected pill segments gain a contrasting border. The parent's selected semantics remain the spoken state. |
| P2 | A filled search field lost its placeholder-based accessible name. | `SearchField` keeps its localizable placeholder as its accessible name after typing; the clear action remains separately named and operable. |
| P2 | Optional status and notice titles were styled as headings without heading semantics. | Both expose heading semantics while preserving polite announcements for warnings and errors. |

## Verification coverage

- Selection, keyboard activation, disabled actions, nested secondary actions, accessible names, and
  minimum targets: `AccessibilityAuditTest`, plus the existing component interaction suites.
- Visible focus: `AccessibilityRegressionTest`; the new cases fail against the original selectable
  card implementation.
- Rendered card, outlined-button, and split-pane boundaries: `ControlContrastTest`, including normal
  and focused states on page and card surfaces, both themes and text sizes.
- Pure color mapping: `TokenAdaptersTest`, including tonal and inline status foregrounds.
- Visual review: light, dark, large-text, and RTL selection screenshots, alongside the affected
  component-family goldens. Selected/unselected/disabled cards appear beside a static card.
- Catalog layout: `CatalogUiTest` renders every family at normal/double text size in LTR and RTL.
  These layout checks do not prove that every possible caller-provided label fits.
- Existing suites exercise form errors and secret masking, named icon controls, selected/toggle
  semantics, navigation and split-pane keyboard actions, loading overlays hiding blocked content,
  polite feedback, reduced-motion overrides, and scroll behavior.
- Source review also checked caller-owned slots and labels in navigation, assistant components,
  tooltips, paginated/showcase content, progress/state views, containers, and `CachedAsyncImage`.
  Informative images require a description; decorative images may use `null`.

## Limits and host responsibilities

This is a source and automated desktop-rendering audit, not a WCAG conformance certification.
VoiceOver and TalkBack announcement order, native accessibility bridges, platform focus restoration,
physical touch behavior, and browser assistive-technology behavior still need manual verification
on their respective platforms. Those checks were not performed in this audit.

Consuming apps supply meaningful localized labels and icon descriptions, accessible custom slot
content, sensible focus order between screens, and custom themes that retain the documented contrast
relationships. Image descriptions, long translations, platform text-size extremes, and layouts at
widths other than the tested catalog widths also require host-level checks. Disabled controls are
excluded from enabled-control contrast requirements. The documented compact segment density
intentionally relaxes minimum target height and remains appropriate only for dense secondary UI.
