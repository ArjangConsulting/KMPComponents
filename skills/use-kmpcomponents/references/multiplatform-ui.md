# Multiplatform UI contracts and verification

Use this guide when a change affects layout, interaction, accessibility, or platform integration.
Apply the relevant sections to the requested change; a copy edit does not need a full device audit.
These principles also apply to shared Compose UI outside KMPComponents. Consumers keep app changes
in their app; contributing to this library follows its own repository instructions.

## Share behavior, assign platform ownership

Keep values, state transitions, validation, and reusable component behavior in common code.
The host owns its window, system bars, native clipboard integration, app identity, and launcher
assets. Use a callback or a small platform adapter for an actual native effect; do not introduce
platform branches merely because a bug appeared on one platform.

Fix the owner of the failure, not just the catalog example where it was noticed. Shared label,
gesture, or semantic behavior belongs in the reusable component; shared color-role mapping belongs
in its theme adapter. A generated sample illustration, app-owned composer, or native system-bar
effect belongs in that example or host. Reproduce with the real component before choosing the layer.

Give each boundary an observable contract. Where system bars overlay the app's themed surface,
keep icon appearance aligned with that resolved theme rather than only the OS setting. If a screen
uses a different backdrop, choose icon appearance for that backdrop. Test an explicit app theme
opposite to the OS theme. A window icon, a running Dock/taskbar icon, and a packaged launcher icon are separate
outputs: verify the launch path that changed, including direct IDE launches when supported.

Separate entry points and keyboard behavior are part of Compose Multiplatform's
[platform differences](https://kotlinlang.org/docs/multiplatform/compose-platform-specifics.html).
Common source compilation does not establish that these host contracts work.

## Make constraints and environment changes explicit

| Situation | Design decision | Useful regression evidence |
| --- | --- | --- |
| Narrow space or enlarged/translated labels | Let text determine size; use stacking or bounded scrolling when content cannot fit. Keep connected segments at a consistent height. | Actual text layout, word boundaries, overflow, and reaching the last option. A rendered root alone proves little. |
| Live LTR/RTL switching | Use logical start/end geometry and invalidate direction-dependent calculations. Preserve caller-owned selection. | Switch direction without relaunching, then inspect joins, selection, keyboard movement, and scrolling. |
| A child can pan, resize, or drag | Make inactive recognition leave gestures to the parent; conditionally register custom recognizers where appropriate. An inactive callback alone may still consume input. | Drag over the child in a scrolling parent, both with the child interaction inactive and active. |
| A group expands near a viewport edge | Reveal the newly expanded content after layout/animation when user initiated. Preserve position for initial state and search unless the API promises otherwise. | Nested scrolling, an oversized group, and reduced-motion behavior. |
| Text input opens the keyboard | Give the scrolling viewport the appropriate safe-area/IME space and keep the action reachable. | Focus, type, and activate the relevant action with the keyboard open on the affected mobile platforms. |

Key `remember` and input handlers to the inputs that change their behavior. Use `rememberUpdatedState`
for a changing callback that should not restart a long-lived operation. A narrowly scoped `key`
can recreate direction-sensitive framework state when necessary; recreating the entire screen
can hide stale state by discarding unrelated user state.

Prefer standard clickable/selectable/scrollable controls over raw pointer handling when they fit.
When custom recognition is needed, check consumption and parent cooperation rather than only the
child's callback. Compose documents these differences in its
[gesture handling guide](https://developer.android.com/develop/ui/compose/touch-input/pointer-input/understand-gestures).

## Treat visual, interactive, and semantic geometry separately

A thin divider can have a generous drag target. A selected card needs a check, border, or other
shape cue as well as tint, plus a semantic selected state. Use the host's touch-target minimum
independently of visible glyph dimensions, and leave spacing between neighboring actions.

A cropped node rectangle is not necessarily the full target or the effective hit region. Reveal
a partially offscreen control before measuring it. Inspect parent bounds or touch delegation
when a small visible node is inside a larger interactive region; retain uncertainty when those
properties are unavailable.

Keep the name, role, state, and actions on the item that actually receives focus. A focusable
image container announcing zoom still needs the image's meaningful name. Merge coherent content
when appropriate, while retaining independent actions such as Copy or Close. Do not add
`contentDescription` to every text control merely because an external exporter lost its label.
Compose's [semantics guide](https://developer.android.com/develop/ui/compose/accessibility/semantics)
explains the role of semantic properties and merging; inspect the native export separately.

Measure meaningful graphics and text against their actual surrounding surface. Matching theme
roles help, but generated bitmap/Canvas content needs its own review: a native contrast audit can
pass while internal image shapes remain indistinguishable. Do not apply control-boundary contrast
rules indiscriminately to photographs or purely decorative content.

## Get the cheapest decisive evidence early

1. Reproduce the changed behavior in a small fixture using the real component. For a confirmed
   regression, prefer a test that fails before the fix and checks the observable outcome afterward.
   Examples include intact words, successful selection, or parent scrolling through a child.
2. Probe the affected native path early, before expanding or repeatedly recording a large gallery.
   A shared interaction change usually merits a bounded Android and iOS check; a desktop-only
   entry-point change needs its desktop launch path. Include desktop/browser interaction checks
   when those targets use the changed input, scroll, or focus path.
3. Broaden verification after the fix: review intended goldens and run the repository's required
   checks. Repeat broad checks when code changes or unresolved failures justify them, rather than
   rerunning an unchanged matrix after every diagnostic query.

Select conditions from the failure risk, rather than taking every cross product:

- Layout: narrow width, long labels, normal/200% catalog text, LTR and a live switch to RTL.
- Color/focus: light and dark on the actual surfaces; alternate/custom theme if its roles changed.
- Gestures/scrolling: nested viewport, inactive/active child recognizer, final item, and expansion.
- Host integration: app versus OS appearance, IME, launch mode, and the native resource being used.
- OS text-size behavior: actual platform settings when requested; a catalog multiplier does not
  establish full Android nonlinear font scaling or iOS Dynamic Type coverage.

## Report what each check establishes

| Evidence | Establishes | Does not establish |
| --- | --- | --- |
| Compilation and ABI checks | The target compiles and public signatures match the reviewed contract. | Layout, usable controls, native window effects, or accessible task completion. |
| Pure/JVM Compose tests | Specified logic, rendered layout, and Compose semantic/interaction assertions. | Identical Android/iOS native exports or screen-reader speech. |
| Reviewed screenshots/pixel measurements | The captured appearance or measured region in that state. | An unseen state, actual hit regions, or spoken reading order. |
| Exported hierarchy and heuristic audit | The properties and rules that provider actually returned/evaluated. | Missing raw properties being absent from the app, or a complete native audit. |
| Native audit | Its evaluated categories for the captured presentation. | Unchecked image regions, other screens/states, or screen-reader task completion. |
| Assistive-technology journey | The traversal, speech, and activation actually observed/asserted. | Whole-app conformance beyond that journey. |

Automated checks complement, rather than replace, assistive-technology testing; see the
[Compose accessibility testing guidance](https://developer.android.com/develop/ui/compose/accessibility/testing).
Record unsupported, unavailable, timed-out, and untested checks separately from pass/fail.
A zero-finding report with incomplete coverage is not a comprehensive accessibility pass.

Follow the consuming project's device workflow. When Amoo is available, discover its installed
schema and audit capabilities; do not assume provider support from a tool's name or an earlier
run. Record the build, device/OS, theme, direction, text setting, captured state, provider coverage,
and evidence. Native findings without target identity/bounds need review before assigning an app
fix. Preserve separate occurrences when diagnosing an exporter that collapses distinct controls.

After a mutation times out, inspect the postcondition before repeating it: the gesture may already
have executed. Distinguish action dispatch, app foreground identity, rendered UI, semantic readiness,
and device/ADB health. If the device is unresponsive, stop the bounded verification attempt,
restore/release owned settings and runtimes where possible, and report the remaining coverage gap.
Do not turn a tool failure into an app defect or an unsupported check into a pass.

## Keep the build usable from the IDE too

Treat Kotlin, Compose, AGP, Gradle, SDK requirements, and supported IDEs as a compatibility graph.
Use the project's declared baseline and resolved dependency metadata when making a build change;
do not combine individually newer versions without checking the resulting set. An older compile
SDK may be impossible because a transitive dependency requires a newer one.

Command-line success is not proof of IDE import, a packaged launcher, or a previously running app
loading new resources. Name the launch/build path that was verified, restart the affected sample
when its loaded resources changed, and keep version requirements with the app's setup instructions.
