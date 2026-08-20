# KMPComponents

Reusable Compose Multiplatform components for Android, desktop, and iOS. The library adapts the
platform-neutral tokens from [`kommon`](https://github.com/maniramezan/kommon); it does not define
a competing design system.

The initial API includes a token-backed Material theme, tri-state theme selection, section cards,
and reusable provider configuration fields suitable for AI and other authenticated services.

```kotlin
KmpTheme(mode = ThemeMode.SYSTEM) {
    ProviderConfigurationForm(
        state = providerState,
        onStateChange = ::updateProvider,
    )
}
```

Local sibling builds use Gradle composite substitution for `kommon`. Published consumers resolve
`io.github.maniramezan.kommon:design-system` normally.
