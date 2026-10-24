# UI verification and honest reporting

Learned in: KMPComponents. Apply the sections that match the change; a copy edit does not need a device
audit.

## Assign platform ownership

- Keep values, state transitions, validation, and reusable behavior in common code. The host owns its
  window, system bars, native clipboard integration, app identity, and launcher assets.
- Use a callback or a small platform adapter for a real native effect. Do not add platform branches
  merely because a bug showed up on one platform.
- Fix the owner of the failure, not the example where it was noticed: shared label, gesture, or semantic
  behavior belongs in the component; color-role mapping belongs in the theme adapter; a sample
  illustration or system-bar effect belongs in the sample or host. Reproduce with the real component
  before choosing the layer.
- Where system bars overlay a themed surface, align icon appearance with the resolved app theme, not
  only the OS setting. Test an explicit app theme opposite to the OS theme.
- A window icon, a running Dock/taskbar icon, and a packaged launcher icon are separate outputs. Verify
  the launch path that changed.

## Constraints and environment changes

| Situation | Design decision | Regression evidence |
| --- | --- | --- |
| Narrow space, enlarged or translated labels | Let text determine size; stack or scroll within bounds. Keep connected segments one height. | Actual text layout, word boundaries, reaching the last option. A rendered root alone proves little. |
| Live LTR/RTL switch | Use logical start/end geometry and invalidate direction-dependent calculations. | Switch direction without relaunching; check joins, selection, keyboard movement, scrolling. |
| A child can pan, resize, or drag | Inactive recognition must leave gestures to the parent; register custom recognizers conditionally. | Drag over the child in a scrolling parent, with the interaction inactive and active. |
| A group expands near a viewport edge | Reveal new content after layout when user initiated; preserve position for initial state and search. | Nested scrolling, an oversized group, reduced motion. |
| Text input opens the keyboard | Give the viewport safe-area/IME space and keep the action reachable. | Focus, type, and activate the action with the keyboard open on mobile. |

Key `remember` and input handlers to the inputs that change their behavior. Use
`rememberUpdatedState` for a changing callback that should not restart a long-lived operation. Prefer
standard clickable/selectable/scrollable controls over raw pointer handling; when custom recognition is
needed, check consumption and parent cooperation, not only the child's callback.

## Visual, interactive, and semantic geometry differ

- A thin divider can have a generous drag target. Use the host's minimum touch target independently of
  the visible glyph, and leave spacing between neighboring actions.
- A selected item needs a shape cue (check, border) and a semantic selected state, not tint alone.
- A cropped node rectangle is not the effective hit region. Reveal a partly offscreen control before
  measuring it, and keep uncertainty when parent bounds are unavailable.
- Keep name, role, state, and actions on the item that actually receives focus. Merge coherent content,
  but keep independent actions (Copy, Close). Do not add `contentDescription` to every text control just
  because an external exporter lost a label.
- Measure meaningful graphics and text against their real surface. Generated bitmap/Canvas content needs
  its own contrast review; a native audit can pass while internal image shapes are indistinguishable.

## Cheapest decisive evidence first

1. Reproduce in a small fixture with the real component. For a confirmed regression, prefer a test that
   fails before the fix and asserts the observable outcome after.
2. Probe the affected native path early, before recording a large gallery. A shared interaction change
   usually merits a bounded Android and iOS check; a desktop-only change needs its desktop launch path.
3. Broaden after the fix: review intended goldens, then run the repo's required checks. Do not rerun an
   unchanged matrix after every diagnostic.

Pick conditions from the failure risk, not the full cross product: layout (narrow, long labels, 100% and
200% text, LTR and live RTL), color/focus (light and dark on the real surfaces), gestures (nested
viewport, final item, expansion), host integration (app vs OS appearance, IME, launch mode). A catalog
text multiplier does not establish Android nonlinear font scaling or iOS Dynamic Type.

## Report what each check establishes

| Evidence | Establishes | Does not establish |
| --- | --- | --- |
| Compilation and ABI checks | The target compiles; public signatures match the reviewed contract. | Layout, usable controls, native window effects, accessible task completion. |
| Pure/JVM Compose tests | Specified logic, rendered layout, Compose semantics and interaction. | Identical Android/iOS native exports or screen-reader speech. |
| Reviewed screenshots | The captured appearance in that state. | An unseen state, real hit regions, spoken reading order. |
| Exported hierarchy and heuristic audit | The properties and rules that provider returned. | That a missing raw property is absent from the app; a complete native audit. |
| Native audit | Its evaluated categories for the captured presentation. | Unchecked image regions, other screens, screen-reader task completion. |
| Assistive-technology journey | The traversal, speech, and activation actually observed. | Whole-app conformance beyond that journey. |

Ordered from cheapest to most conclusive, each row proves more about real use than the one above it. A
shared interaction change merits at least a bounded Android and iOS check; if it was not run, report it
as a gap rather than substituting a higher row's wording.

Partial-verification report template:

```text
Ran: <exact commands> -> <result>
Establishes: <what those results prove, per the table>
Not run: <check> - <reason: sandbox, no device, unavailable host>; covered by CI: <yes/no/unknown>
Gesture/state evidence still missing: <the specific scenario>
```

- Record unsupported, unavailable, timed-out, and untested checks separately from pass/fail. A
  zero-finding report with incomplete coverage is not a comprehensive accessibility pass.
- Record build, device/OS, theme, direction, text setting, captured state, provider coverage, and
  evidence paths. Findings without target identity or bounds need review before assigning an app fix.
- After a mutation times out, inspect the postcondition before repeating it; the gesture may have
  executed. Distinguish dispatch, foreground app identity, rendered UI, semantic readiness, and device
  health. If the device is unresponsive, stop the bounded attempt, restore what you own, and report the
  gap. Do not turn a tool failure into an app defect or an unsupported check into a pass.
