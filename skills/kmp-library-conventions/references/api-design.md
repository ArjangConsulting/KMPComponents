# Public API design for a KMP/Compose library

Learned in: KMPComponents, kmp-code-editor, kommon.

## Declarations

- `explicitApi()` is on (kommon: `explicit-api = strict` for non-test compilations). Every public
  declaration needs `public` and an explicit type.
- KDoc on every public declaration that is not self-explanatory. Document the non-obvious: masking,
  when errors show, what is announced, what state the component keeps internally.
- A checked-in ABI baseline guards the surface. KMP modules use the Kotlin Gradle plugin's
  `abiValidation()` with `checkKotlinAbi` / `updateKotlinAbi` (KMPComponents, kommon `:design-system`);
  Android-library modules use Binary Compatibility Validator `apiCheck` / `apiDump` (kommon). Review the
  baseline diff, commit it with the code, and treat removed or changed signatures as breaking. Empty
  dumps are a failure, not a pass.
- One public type per file, named after the file, unless the types are a tiny sealed family.
- Value types that can be mis-declared `require(...)` their invariants in `init` so misconfiguration
  fails at startup.

## Composables

1. **Stateless first.** Take `value` + `onValueChange` (or `state` + `onStateChange`). Keep only
   ephemeral UI state internal (a reveal toggle, popup/expansion state) and document it.
2. **Parameter order:** required, then `modifier: Modifier = Modifier`, then optional parameters, then
   a trailing `content` lambda. Apply `modifier` to the root only.
3. **No user-visible string without a parameter.** English defaults are fine, exposed as parameters or
   an `@Immutable data class XxxLabels` for multi-string components.
4. **Theme through roles, not literals.** No literal colors, `dp` spacing, or `sp` sizes (a 1.dp border
   is the accepted exception). Size glyphs from typography so they follow the user's font scale.
5. **Compose-stable models.** Public state and label classes are `@Immutable data class`es with `val`s;
   enum entries are SCREAMING_CASE. Classes holding secrets override `toString()` to redact them.
6. **Decompose into public parts.** Build larger components from smaller public ones so apps can reuse
   the pieces. If an existing component hides a reusable piece, extract it first.
7. **Pure logic out of composables.** Validation, mapping, and formatting are plain functions with
   `commonTest` coverage. Test names state the invariant they protect.
8. **Accessibility baseline.** Headings use `semantics { heading() }`; dynamic warnings and errors use
   a polite `liveRegion`; icon-only controls have a name and tooltip; selection has a shape cue
   (check, border) plus a semantic selected state, not tint alone.

## Naming

- Reuse the sibling libraries' names (ComposeUIComponents, SwiftUIComponents) so the three stay
  parallel.
- If a name collides with a Material 3 or `androidx.compose.ui.window` composable, prefix it with
  `Theme` (`ThemeSwitch`, not `Switch`) so a consuming app never resolves the unthemed Material one by
  accident. Tell consumers to import the wrapper explicitly.
- Record new name mappings in the README.

## Library behavior (non-UI)

- Cancellation is never swallowed: anything catching `Throwable` or using `runCatching` around
  suspending work rethrows `CancellationException` first.
- Background work launched into a library- or caller-owned scope contains and logs failures; it must not
  crash the host.
- Telemetry, analytics fan-out, and logging never change the outcome of the primary operation.
- Inject time (`clockMs: () -> Long`) so tests are deterministic.
- DI-agnostic: plain constructor parameters, no DI annotations on library classes.
- Prefer pure-Kotlin over `android.*` APIs when behavior allows it. Consumers' plain JVM tests run with
  stubs that silently return `null`/`0`.
- Put logging behind a seam (a small logger interface with a no-op default) rather than importing a
  concrete logger.

## Scope

- Keep the library product-agnostic. Catalog/sample examples use generic copy and temporary local
  state, label simulations as simulations, and never copy a consuming app's workflow, storage,
  credentials, or backend.
- A heavy optional dependency (an image loader, a language server) goes in a separate artifact so the
  core module stays free of it. Keep its types out of the public API (`implementation`, not `api`).
- When extracting from a product app, split the generic contract from the concrete adapters, DTOs, and
  product fields, and leave the latter in the app.
