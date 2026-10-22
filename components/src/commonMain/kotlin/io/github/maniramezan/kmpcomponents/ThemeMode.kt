package io.github.maniramezan.kmpcomponents

/** User-facing appearance preference. Persist this value rather than a resolved light/dark flag. */
public enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Resolves this preference to a concrete dark/light decision given the platform setting. */
public fun ThemeMode.isDark(systemInDarkTheme: Boolean): Boolean = when (this) {
    ThemeMode.SYSTEM -> systemInDarkTheme
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

/**
 * The explicit mode that flips what is currently shown, for a single light/dark toggle button.
 *
 * The result is always [ThemeMode.LIGHT] or [ThemeMode.DARK], never [ThemeMode.SYSTEM], so toggling
 * while following the system pins the opposite of the current system appearance.
 */
public fun ThemeMode.toggled(systemInDarkTheme: Boolean): ThemeMode =
    if (isDark(systemInDarkTheme)) ThemeMode.LIGHT else ThemeMode.DARK
