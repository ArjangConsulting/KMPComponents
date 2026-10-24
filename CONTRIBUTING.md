# Contributing

Thanks for helping improve KMPComponents. [`AGENTS.md`](AGENTS.md) is the source of truth for how the
library is built: module layout, the public API rules every component follows, styling and
accessibility requirements, and the verification commands. It applies to people and coding agents
alike.

## Before you start

- For a new component or a public API change, open an issue first so the name and shape can be
  agreed. Names follow the sibling ComposeUIComponents and SwiftUIComponents libraries.
- Visual values come from kommon's design tokens. Propose a missing token upstream in
  [kommon](https://github.com/maniramezan/kommon) instead of adding one here.

## Making a change

1. Branch from `main`. Pull requests are the only way changes land.
2. Follow [Adding or changing a component](AGENTS.md#adding-or-changing-a-component): source,
   common and desktop UI tests, screenshot goldens, the ABI dump, a catalog entry, and a README row.
3. Run the checks CI runs:

   ```bash
   ./gradlew ktlintCheck :components:lint :components-image:lint
   ./gradlew :components:jvmTest :components:verifyRoborazziJvm :components:checkKotlinAbi
   ./gradlew :components:allTests :components-image:allTests :sample:jvmTest
   ```

4. Review changed screenshot goldens before committing them, and explain any ABI dump change in the
   pull request.

## Commit messages

Commits follow [Conventional Commits](https://www.conventionalcommits.org/), which drive Release
Please: `feat:` for new public API, `fix:` for user-visible fixes, and `docs:`, `test:`,
`refactor:`, `build:`, `ci:`, or `chore:` for everything else. Mark breaking changes with `!` and
describe them in the commit body.

## Reporting bugs

Use the bug report form and include the library version, platform, and a minimal reproduction.
Report security problems privately as described in [SECURITY.md](SECURITY.md).
