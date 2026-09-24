package io.github.maniramezan.kmpcomponents

/** User-facing appearance preference. Persist this value rather than a resolved light/dark flag. */
public enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Resolves this preference to a concrete dark/light decision given the platform setting. */
public fun ThemeMode.isDark(systemInDarkTheme: Boolean): Boolean = when (this) {
    ThemeMode.SYSTEM -> systemInDarkTheme
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}
