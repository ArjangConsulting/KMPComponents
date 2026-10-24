# Releases and publishing

Learned in: KMPComponents, kommon, kmp-code-editor. Tag format, branching, and automation tooling are
maintainer choices; these repos' choices are listed as a working example. Confirm them before applying
to a new repo.

## Versions and tags

- These repos use bare SemVer everywhere: `0.1.0`, never `v0.1.0`. This covers tags, GitHub Releases,
  release-workflow tag globs, and dependency pins.
- Never hand-edit versions. `VERSION_NAME` is injected by `release.yml` from the tag, which must equal
  `.release-please-manifest.json`.
- Per-tag notes live in GitHub Releases. Do not add a `CHANGELOG.md`.

## Commits and Release Please

- Conventional Commits drive Release Please. Only `feat:` (minor while pre-1.0), `fix:` (patch), and
  `!` (breaking) produce a release. Use `docs:`, `test:`, `refactor:`, `build:`, `ci:`, `chore:` for
  everything else so the notes list only user-visible changes.
- Pre-1.0, breaking public API changes are allowed but must be called out in the commit body, and an ABI
  dump diff should explain them.

## Branching

- PR-based repos: never commit to `main`; branch first (`feature/<name>`, `fix/<name>`), push and open a
  PR only when asked.
- A repo may declare an exception before its first release (kmp-code-editor pushes to `main` until
  0.1.0). Read that repo's `AGENTS.md`; do not generalize the exception.
- With stacked PRs, after a squash merge rebase the next branch with
  `git rebase --onto origin/main <old base sha>` and push with `--force-with-lease`.

## Maven Central

- Publishing uses `com.vanniktech.maven-publish` plus Dokka. `GROUP` and POM metadata live in
  `gradle.properties`.
- `release.yml` needs four secrets in the `secrets` environment: `MAVEN_CENTRAL_USERNAME`,
  `MAVEN_CENTRAL_PASSWORD`, `SIGNING_IN_MEMORY_KEY`, `SIGNING_IN_MEMORY_KEY_PASSWORD`.
- `io.github.<org>` namespaces cannot be self-registered. A custom namespace (for example
  `com.arjangconsulting`) must be verified in the Central Portal through a DNS TXT record.
- Release tagging through a GitHub App needs Contents write and Workflows write. Token creation should
  check those permissions before publishing so a missing one cannot strand a Maven release.
- Gate releases with a repository variable (`RELEASES_ENABLED`) until the maintainer has reviewed the
  repo. Do not remove the gate or set the variable.
- Aggregated Dokka output goes to GitHub Pages after CI passes on `main`.
