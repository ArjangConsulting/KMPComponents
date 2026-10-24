# Security policy

## Supported versions

Security fixes go into the latest released minor version on Maven Central. KMPComponents is
pre-1.0, so older minor versions are not patched.

## Reporting a vulnerability

Report vulnerabilities privately through
[GitHub private vulnerability reporting](https://github.com/ArjangConsulting/KMPComponents/security/advisories/new).
Do not open a public issue. Include the affected version, the platform (Android, desktop, iOS, or
web), and steps or a minimal sample that reproduces the problem.

You should get a first response within a week. Once a fix is released, the advisory is published
with credit to the reporter unless you ask otherwise.

The library renders UI and does not store or transmit data itself. Reports about how
`SecretTextField` masks input, how `components-image` fetches URLs, or what the components expose to
accessibility services are in scope.
