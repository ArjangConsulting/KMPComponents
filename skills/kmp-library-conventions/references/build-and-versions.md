# Build, versions, and targets

Learned in: KMPComponents, kommon, kmp-code-editor. Claims marked (as of 2026-10) are version
specific: recheck them against current release notes before relying on them.

## Version management

- Versions live in `gradle/libs.versions.toml` only. Never hardcode them in build scripts.
- Declare Compose libraries through catalog entries (`libs.compose.*`). The Compose plugin's
  `compose.runtime` / `compose.material3` accessors are deprecated at error level in Compose
  Multiplatform 1.12 (as of 2026-10). Material 3 has its own version; keep it on the release the
  Compose plugin pairs with.
- Treat Kotlin, Compose, AGP, Gradle, compile SDK, and supported IDEs as one compatibility graph.
  Do not combine individually newer versions without checking the resulting set; a transitive
  dependency can force a newer compile SDK than you chose.
- Example of the graph biting (as of 2026-10): SDK 37 needs AGP 9.1.1+ and a matching Android Studio.
  Keep AGP in the series the documented IDE baseline supports, and move to another minor only when
  intentionally updating the README's IDE requirement.
- Keep Kotlin/JVM bytecode at one target across sibling repos (`JvmTarget.JVM_17` here) so consumers
  can mix them.
- The ktlint engine version is not tracked by Dependabot; bump it by hand.
- Dependabot opens one weekly grouped PR for Gradle and GitHub Actions. Fix a breaking update on the
  branch rather than pinning around it. KMPComponents ignores `com.android.*` minor and major updates
  in `.github/dependabot.yml`, so an AGP minor bump is always a deliberate change (update the README's
  IDE requirement in the same PR); patch updates flow through.
- When "newest" conflicts with the documented IDE baseline, the baseline wins unless the task is
  explicitly to raise it. Find current stable versions in the Kotlin, Compose Multiplatform, and AGP
  release notes and the Compose/Kotlin compatibility tables, not from memory.

## Targets

- Android, desktop JVM, iOS, and `wasmJs` are the target set. Platform differences stay in small
  `expect`/`actual` adapters (pointer icons, reduced motion, clipboard entries); the rest is common.
- Adding a target means new `actual`s for every existing `expect`, plus engines for dependencies
  (for example a Ktor engine per platform for an image loader).
- `wasmJs` runs `commonTest` in headless Chrome. It declares `binaries.executable()` only so webpack
  bundles Skiko for those tests (CMP-4906, as of 2026-10). The Node.js, Yarn, and Binaryen downloads the
  Kotlin plugin needs are declared as `ivy` repositories (with `includeModule` content filters) in
  `settings.gradle.kts`, because `FAIL_ON_PROJECT_REPOS` rejects the repositories the plugin would
  otherwise add. Then set `downloadBaseUrl` to `null` on the Wasm Node.js, Yarn, and Binaryen env specs
  in the root build script (`plugins.withType<...>` blocks) so the plugin stops adding them itself.
- Adding the target also means: `wasmJsMain`/`wasmJsTest` source sets, a `wasmJs` `actual` for every
  `expect`, a `wasmJs` variant of every dependency that must be consumable (check that sibling
  libraries publish one and from which release), and a refreshed ABI dump (the klib section changes).
- Browser APIs behave differently: CSS resize cursors are internal to Compose, so fall back to the
  hand cursor; clipboard entries use Compose's plain-text helper; reduced motion comes from a
  `prefers-reduced-motion` subscription.
- Decorative looping animations check a single "decorative animations enabled" gate that also turns
  them off in previews.

## Local sibling dependencies

- Wire a sibling checkout with a composite build behind a property (`-PuseLocalKommon=true`, with
  `-PkommonDir=<path>` for another checkout or a git worktree). Keep the published module free of the
  dependency when only a sample needs it.

## Tests and CI

- `jvmTest` is the fast loop (common + Compose UI tests). `allTests` needs macOS + Xcode for iOS and
  Chrome for web. Compose UI tests via `runComposeUiTest` are desktop-only because they need Skiko.
- A typical CI run: format check, `allTests`, ABI check for each published module, screenshot
  verification, coverage, `assemble`, aggregated Dokka, and builds of the desktop, Android, and iOS
  samples. Keep shared IDE run configurations (`.run/`) in sync with the CI task list.
- Screenshot goldens: review a changed golden before committing; a screenshot failure should upload
  the diff images as a CI artifact.
- Coverage tooling differs by repo: KMPComponents uses Kover; kommon (Android-library modules) uses
  JaCoCo with a 70% gate. In kommon's AGP 9 toolchain, JaCoCo does not measure classes exercised only
  through `RobolectricTestRunner` (as of 2026-10). Exclude the known-affected classes from the ratio
  rather than lowering the threshold, and keep writing the real tests.

## Sandboxes

- The build needs Google Maven (`dl.google.com`) for the Android Gradle plugin. A sandbox without it
  cannot configure the project. Say so instead of claiming a change was verified, and list exactly
  which checks did not run (Android lint, Robolectric tests, coverage gate, Dokka, Compose modules,
  KMP target builds) as "not run locally; covered by CI".
- Command-line success is not proof of IDE import, a packaged launcher, or a running app picking up new
  resources. Name the path you verified and restart the affected sample after resource changes.
