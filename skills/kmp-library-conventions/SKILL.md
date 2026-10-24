---
name: kmp-library-conventions
description: Conventions for building, reviewing, and releasing a published Kotlin/Compose Multiplatform library (not an app) - public API rules (explicitApi, ABI dumps, stateless composables, Theme-prefixed names), Gradle/version-compatibility and wasm build gotchas, Maven Central/Release Please publishing, and an evidence ladder for reporting UI verification honestly. Use when adding or changing public API in a KMP library, bumping Kotlin/Compose/AGP versions or adding a target such as wasmJs, wiring CI or releases, or deciding what a test or audit actually proves.
---

# KMP library conventions

Distilled from three libraries (KMPComponents, kommon, kmp-code-editor); where a practice is specific to
one of them, the reference says so and treats it as an example, not a requirement. Each repo's
`AGENTS.md` still wins for repo-specific facts (module map, token sources, sibling-name mappings). When this skill and the
checked-in build disagree, trust `settings.gradle.kts`, `gradle/libs.versions.toml`, and the module
build files, then fix the doc.

Load only the reference the task needs:

| Task | Read |
| --- | --- |
| New or changed public declaration, composable, or model | [api-design.md](references/api-design.md) |
| Version bump, new target, Gradle/CI/IDE breakage, sandbox without Google Maven | [build-and-versions.md](references/build-and-versions.md) |
| Release, tagging, Maven Central, Dependabot, branch policy | [release-and-publishing.md](references/release-and-publishing.md) |
| Layout, gesture, accessibility, or native-host change; reporting what was verified (also read the "Sandboxes" section of build-and-versions.md when checks could not run) | [ui-verification.md](references/ui-verification.md) |

## Rules that apply to every task

1. **Check the checkout, not memory.** Resolve versions, targets, and APIs from the repo's catalog and
   `api/` dumps. Before pinning or recommending a version, verify the latest stable online when
   network is available.
2. **Public API is a contract.** Every public declaration has `public`, an explicit type, and KDoc.
   An ABI dump diff is an API diff; call out breaking changes explicitly in the commit body.
3. **Libraries stay product-agnostic.** Product copy, persistence, networking, and DI belong in the
   consuming app. If a value is missing upstream (a design token), derive it or propose it upstream
   instead of adding a one-off here.
4. **Say what was not verified.** A compile, a JVM test, and a screen-reader journey prove different
   things. Name the ones not run (sandbox, missing device, unavailable host) instead of implying a
   full pass.
5. **Follow the repo's branch and release policy.** In a PR-based repo, branch first and re-check
   `git branch --show-current` before each commit; another session can switch branches in a shared
   checkout. Confirm naming, versioning, and release conventions with the maintainer before baking them
   into a new repo.

## Related skills

When installed alongside them, these skills split the work with this one:

- `use-kmpcomponents` (consumer side of KMPComponents) and `add-component` / `review-component`
  (contributor playbooks in the KMPComponents repo) carry repo-specific steps such as file layout, token
  adapters, and the catalog entry. This skill carries the rules that transfer to any KMP library.
- `kotlin-tooling-agp9-migration` and `kotlin-tooling-cocoapods-spm-migration` (Kotlin agent skills)
  handle the version migrations themselves.

## Keeping this skill honest

Each reference lists the repo it was learned in. Version-specific claims carry an "as of" date; recheck
them before relying on them. When a repo's `AGENTS.md` repeats a rule from here, prefer pointing to
the skill over copying it.
