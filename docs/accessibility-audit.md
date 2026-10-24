# Shared-component accessibility audit

The main audit covers the common Compose implementation and desktop-rendered behavior of the
component catalog. The mobile follow-ups below add iOS and Android runtime evidence for bounded catalog states.
It uses the repository's design style guide and the WCAG 2.2 criteria for
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
| P2 | The generated zoom-preview bitmap used almost indistinguishable row fills, and the original audit did not measure those pixels. | The illustration has high-contrast row outlines and a visible preview caption. `CatalogContrastTest` measures the actual rendered image boundaries; the iOS follow-up records screenshot measurements. |
| P2 | At 200% text, a long key squeezed the copy value into broken word fragments on iOS. | `KeyValueRow` flows the value below the label when its words cannot fit beside it. `ComponentUiTest` checks word boundaries at 100%/200% in LTR/RTL, and the iOS copy action was rechecked. |
| P2 | The focusable zoom container exposed a state without its child image's description. | `ZoomableImage` merges its image description with the zoom state. A regression assertion on the focused node fails before the fix. |
| P2 | The split-pane resize handle occupied only an 8 dp strip. | Both split directions reserve Material's minimum touch target independently of the thin visible line. Interaction tests check dimensions alongside keyboard and accessibility actions. |
| P2 | Android's system-bar icons followed the device theme even when the catalog selected the opposite appearance. | The catalog reports its resolved theme to the Android host, which updates status/navigation icon appearance. Catalog tests check the callback and emulator captures verify Light/Dark/System. |
| P2 | Connected segments split an ordinary label and grew to inconsistent heights at 200% text on a narrow Android viewport. | The shared connected row scrolls at intrinsic label widths. `ActionsUiTest` reproduces the original two-line label and verifies intact labels plus reachable selections in LTR/RTL after the fix. |
| P2 | An unzoomed image preview's drag recognizer swallowed vertical parent scrolling. | The pan recognizer is active only while zoomed. `DesktopExtrasUiTest` reproduces the original blocked scroll and checks parent scrolling at 1× versus internal panning while zoomed. |

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

## iOS catalog follow-up

Amoo reproduced the reported **Copy, zoom, and status bar** screen on an iPhone 18 Pro simulator
running iOS 27.2. The original rows are pixels inside one generated illustration, not separately
interactive controls. Their nearly identical fills made the preview difficult to interpret. The
original source/desktop audit did not cover this generated bitmap and was insufficient evidence
for its appearance on iOS.

The preview now has a visible caption and outlines derived from `onSecondaryContainer`. Measurements
from flat sRGB-tagged screenshot pixels were:

| Theme | Original row fill/background | New outline/fill | New outline/background |
| --- | --- | --- | --- |
| Light | 1.013:1 | 13.12:1 | 13.28:1 |
| Dark | 1.470:1 | 11.00:1 | 7.48:1 |

The outline measurements held at both 100% and 200% **catalog text settings** in the default theme.
These are screenshot estimates using the WCAG relative-luminance formula, not calibrated physical
display measurements. Sampled nearby Notes labels exceeded 4.5:1 and field outlines exceeded 3:1.

Apple's current-screen native contrast audit, invoked through Amoo, reported no contrast issues
in light/dark at both catalog text settings. It also passed on the original faint illustration:
its result did not establish contrast within the image's internal pixels. The separate screenshot
measurements and rendered-image regression test cover that gap.

The large-text pass also exposed fragmented wrapping in the copy row. After the shared
`KeyValueRow` fix, the full value wraps at words below its label in both themes at 200%. The copy
control remained visible/tappable, and its `Copied` feedback was asserted. Appearance and catalog
text settings were restored to System/100%, and the leased simulator runtime was released.

Expanded audits used an iPhone 18 Pro Max simulator on the same iOS version after an unrelated
Xcode test runner occupied the original simulator. Amoo's current-screen heuristic evaluated
`UX-001` and `UX-002`; its zero findings are a heuristic result, with a `notAssessed` verdict.
The native audit additionally evaluated `hitRegion`, `sufficientElementDescription`, `textClipped`,
`trait`, and `elementDetection` in scrolled presentations of the preview and lower controls.

Before the final fixes, native auditing reported a missing description with an empty name/ID and
no element bounds. Scrolling the preview offscreen removed that finding, but did not establish its
identity. Independently, source and a failing semantic regression assertion showed that the
focusable `ZoomableImage` root lacked its child image's description. Its name and zoom state are
now merged. The native audit also identified the named `Resize panes` handle as only 8 points high;
both split-pane directions now reserve the Material minimum touch target.

On the rebuilt app, the named image container exports both its description and `Zoom 100%`, and
the vertical resize handle measures 48 points high. The missing-description native finding still
has no element identity or bounds and remains unresolved; the naming fix must not be presented
as resolving that anonymous finding. A lower scrolled presentation also reports
`v1.2.0` as "Label not human-readable". This is intentionally displayed sample version data and
needs contextual assistive-technology review. The final native audit returned `fail` in all five captured presentations: light/dark at 100%
near the top, light 100% lower, light 200% preview, and dark 200% lower. Each reported the anonymous
description issue; light 100% lower additionally reported the version warning. No issues were
reported for contrast, hit regions, clipping, traits, or element detection in those presentations.
These findings prevent a blanket native-audit pass. The final run restored System appearance and
100% catalog text, released its runtime, and changed no global accessibility settings.

This follow-up is limited to this screen in the default theme, LTR, and the catalog's font-scale
control. It does not establish a full Dynamic Type, VoiceOver, TalkBack, browser, or whole-app audit.

## Android catalog follow-up

Amoo installed and launched the sample APK built with AGP 9.1.1 on an Android 15/API 35
emulator: 1080 × 2400 pixels, 420 dpi (density 2.625), original system font scale 1.0.
The normal-text sweep covered the top presentation of all 14 catalog families in the default
light theme, plus an opened confirmation dialog. Cancel dismissed the dialog; its captured tree
excluded underlying app controls. This is ordinary UI/tree evidence, not TalkBack traversal proof.
Targeted checks added appearance, RTL, and the Desktop helpers preview/copy row at 200% catalog
text. Catalog text multipliers do not verify Android's full OS/nonlinear font-scaling behavior.

Amoo's `audit_accessibility` evaluated only the `UX-001` naming and `UX-002` bounds heuristics.
Its verdicts were `notAssessed`, with repeated unnamed-control and small-bounds findings.
Android's exported tree flattened descendants, separated visible labels from anonymous controls,
and omitted selected/state-description data. These findings need review; empty exported fields
do not establish missing native spoken names. For example, Reset's visible button node measured
40 dp high beside a co-located 48 dp outer node; actual hit-region coverage was unavailable.
The vertical split-pane handle measured 48 dp high when fully visible.

`audit_accessibility_native` returned `unsupported` with no evaluated Android checks.
`test_voiceover` also returned `unsupported`, and the installed schema exposed no TalkBack
counterpart. No Android native/ATF, contrast, TalkBack speech, reading-order, or announcement
pass is claimed. The heuristic missed both confirmed visual defects below, which were found
through screenshots and app interaction.

Selecting catalog Light on a dark-system emulator left white status-bar icons on a light surface.
The Android host now uses the resolved catalog appearance for status/navigation icon styles;
Light/Dark/System screenshots verify the corrected status-bar appearance. At 200% text, the
connected theme row also split “System” into “Syste”/“m” and broke the joined outline. The shared
row now preserves intrinsic label widths and scrolls when constrained. A JVM regression test
fails before the change and passes in LTR/RTL afterward. Final emulator captures at 200% confirm
uniform single-line labels in both directions; horizontal scrolling reveals Dark and selecting it
changes the theme. Some gesture calls timed out after the gesture had executed, so subsequent
queries and screenshots were used to verify the outcome.

Desktop helpers' copy value wraps at words at 200%, and an Android clipboard overlay showed the
full copied fixture text. Amoo did not export the state description, and `assert_value` returned
a deadline error without the last observed value, so its `Copied` assertion could not be verified. Likewise, changing
card selection visibly moved the checkmark while `tap_element` incorrectly described the screen
as identical and `assert_selected` correctly reported unavailable state. These are provider/evidence
limitations, not proof that the underlying component failed its action.

The attempted large-text route from Desktop helpers back to the section picker failed in Amoo's
`scroll_to_element`; the subsequent expanded-selection-group check is unverified on Android.
A later touch regression test confirmed that `ZoomableImage` intercepted parent drags even when
unzoomed. The app now leaves those drags to the parent and activates panning only while zoomed;
the original navigation failure must not be attributed solely to Amoo. Its final Android follow-up
could not execute the gesture because launch assertions and even ADB-backed screenshot capture
timed out on the emulator. The pan fix has JVM touch-test coverage and Android build verification;
its final Android device behavior remains unverified.
The normal Choices presentation was included in the sweep. With the keyboard open on Chat,
screenshots confirmed that Message and Send remained visible. The heuristic audit and later Send
and keyboard-dismiss calls timed out; message completion is unverified. There is no claim that
every control or scrolled state in each family was audited. App appearance/text/direction were
restored to System/100%/LTR before the IME run; Amoo terminated the app and released that runtime.
No OS font or accessibility-service settings were manually changed.

## Limits and host responsibilities

This source, desktop-rendering, and bounded mobile audit is not a WCAG conformance certification.
VoiceOver and TalkBack announcement order, broader native bridge behavior, platform focus restoration,
physical-device touch behavior, and browser assistive-technology behavior still need manual verification
on their respective platforms. Those checks were not performed in this audit.

Consuming apps supply meaningful localized labels and icon descriptions, accessible custom slot
content, sensible focus order between screens, and custom themes that retain the documented contrast
relationships. Image descriptions, long translations, platform text-size extremes, and layouts at
widths other than the tested catalog widths also require host-level checks. Disabled controls are
excluded from enabled-control contrast requirements. The documented compact segment density
intentionally relaxes minimum target height and remains appropriate only for dense secondary UI.
