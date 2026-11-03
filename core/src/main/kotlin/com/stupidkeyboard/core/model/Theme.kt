package com.stupidkeyboard.core.model

/**
 * Colour palette used to draw the keyboard surface.
 *
 * All colours are packed ARGB integers so that this module stays free of Android
 * dependencies and the palettes can be shared by the IME (custom View rendering) and
 * the companion app (Compose).
 */
data class KeyPalette(
    val background: Int,
    val keyBackground: Int,
    val keyBackgroundPressed: Int,
    val keyText: Int,
    val modifierKeyBackground: Int,
    val modifierKeyBackgroundPressed: Int,
    val modifierKeyText: Int,
    val accent: Int,
    val accentText: Int,
    val spaceKeyBackground: Int,
    val hintText: Int,
    val divider: Int,
    val toolbarBackground: Int,
    val toolbarText: Int,
    val previewBackground: Int,
    val previewText: Int,
    val shadow: Int,
)

/** A complete keyboard appearance. */
data class KeyboardTheme(
    /** Stable identifier persisted in preferences. */
    val id: String,
    val name: String,
    /** True when the palette is intended for dark environments. */
    val isDark: Boolean,
    val palette: KeyPalette,
)

object ThemeCatalog {

    const val AUTO = "auto"
    const val LIGHT = "light"
    const val DARK = "dark"
    const val MIDNIGHT = "midnight"
    const val SAND = "sand"

    val Light = KeyboardTheme(
        id = LIGHT,
        name = "Daylight",
        isDark = false,
        palette = KeyPalette(
            background = 0xFFE9EBF0.toInt(),
            keyBackground = 0xFFFFFFFF.toInt(),
            keyBackgroundPressed = 0xFFD7DBE4.toInt(),
            keyText = 0xFF14161B.toInt(),
            modifierKeyBackground = 0xFFD3D8E2.toInt(),
            modifierKeyBackgroundPressed = 0xFFBFC6D4.toInt(),
            modifierKeyText = 0xFF2B303A.toInt(),
            accent = 0xFF3B5BFF.toInt(),
            accentText = 0xFFFFFFFF.toInt(),
            spaceKeyBackground = 0xFFF5F6F9.toInt(),
            hintText = 0xFF6B7280.toInt(),
            divider = 0x14000000,
            toolbarBackground = 0xFFF3F4F8.toInt(),
            toolbarText = 0xFF2B303A.toInt(),
            previewBackground = 0xFFFFFFFF.toInt(),
            previewText = 0xFF14161B.toInt(),
            shadow = 0x1F000000,
        ),
    )

    val Dark = KeyboardTheme(
        id = DARK,
        name = "Graphite",
        isDark = true,
        palette = KeyPalette(
            background = 0xFF101216.toInt(),
            keyBackground = 0xFF23262D.toInt(),
            keyBackgroundPressed = 0xFF353A44.toInt(),
            keyText = 0xFFF2F4F8.toInt(),
            modifierKeyBackground = 0xFF191C22.toInt(),
            modifierKeyBackgroundPressed = 0xFF2C313A.toInt(),
            modifierKeyText = 0xFFB9C0CC.toInt(),
            accent = 0xFF7C93FF.toInt(),
            accentText = 0xFF0B0D11.toInt(),
            spaceKeyBackground = 0xFF1B1E24.toInt(),
            hintText = 0xFF8A93A3.toInt(),
            divider = 0x1AFFFFFF,
            toolbarBackground = 0xFF14171C.toInt(),
            toolbarText = 0xFFC9D0DB.toInt(),
            previewBackground = 0xFF2C313A.toInt(),
            previewText = 0xFFF2F4F8.toInt(),
            shadow = 0x66000000,
        ),
    )

    val Midnight = KeyboardTheme(
        id = MIDNIGHT,
        name = "Midnight",
        isDark = true,
        palette = KeyPalette(
            background = 0xFF0A1020.toInt(),
            keyBackground = 0xFF16203A.toInt(),
            keyBackgroundPressed = 0xFF22315A.toInt(),
            keyText = 0xFFE8EEFF.toInt(),
            modifierKeyBackground = 0xFF0F1729.toInt(),
            modifierKeyBackgroundPressed = 0xFF1C2744.toInt(),
            modifierKeyText = 0xFF9FB2D9.toInt(),
            accent = 0xFF4ED1C7.toInt(),
            accentText = 0xFF04131A.toInt(),
            spaceKeyBackground = 0xFF111A2E.toInt(),
            hintText = 0xFF7F90B5.toInt(),
            divider = 0x1A8FB4FF,
            toolbarBackground = 0xFF0C1322.toInt(),
            toolbarText = 0xFFC2D1EE.toInt(),
            previewBackground = 0xFF22315A.toInt(),
            previewText = 0xFFE8EEFF.toInt(),
            shadow = 0x66000000,
        ),
    )

    val Sand = KeyboardTheme(
        id = SAND,
        name = "Sandstone",
        isDark = false,
        palette = KeyPalette(
            background = 0xFFF1EAE0.toInt(),
            keyBackground = 0xFFFFFBF5.toInt(),
            keyBackgroundPressed = 0xFFE4D8C8.toInt(),
            keyText = 0xFF2A241C.toInt(),
            modifierKeyBackground = 0xFFE2D7C8.toInt(),
            modifierKeyBackgroundPressed = 0xFFD0C1AC.toInt(),
            modifierKeyText = 0xFF3B3226.toInt(),
            accent = 0xFFC2683A.toInt(),
            accentText = 0xFFFFFBF5.toInt(),
            spaceKeyBackground = 0xFFF8F2E9.toInt(),
            hintText = 0xFF8A7A66.toInt(),
            divider = 0x1A3B3226,
            toolbarBackground = 0xFFEDE6DB.toInt(),
            toolbarText = 0xFF3B3226.toInt(),
            previewBackground = 0xFFFFFBF5.toInt(),
            previewText = 0xFF2A241C.toInt(),
            shadow = 0x1F3B3226,
        ),
    )

    val presets: List<KeyboardTheme> = listOf(Light, Dark, Midnight, Sand)

    /** Resolves a stored theme preference, following the system when set to [AUTO]. */
    fun resolve(themeId: String?, systemDark: Boolean): KeyboardTheme = when (themeId) {
        null, AUTO -> if (systemDark) Dark else Light
        LIGHT -> Light
        DARK -> Dark
        MIDNIGHT -> Midnight
        SAND -> Sand
        else -> if (systemDark) Dark else Light
    }

    fun byId(id: String?): KeyboardTheme = presets.firstOrNull { it.id == id } ?: Light
}
