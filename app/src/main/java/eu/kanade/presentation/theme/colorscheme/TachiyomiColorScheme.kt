package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Zink's default palette: charcoal surfaces, electric teal, and a lavender accent.
 */
internal object TachiyomiColorScheme : BaseColorScheme() {

    override val darkScheme = darkColorScheme(
        primary = Color(0xFF52D9CA),
        onPrimary = Color(0xFF003731),
        primaryContainer = Color(0xFF15524D),
        onPrimaryContainer = Color(0xFFA4F4E9),
        inversePrimary = Color(0xFF006B60),
        secondary = Color(0xFF52D9CA), // Unread badge
        onSecondary = Color(0xFF003731),
        secondaryContainer = Color(0xFF15524D),
        onSecondaryContainer = Color(0xFFA4F4E9),
        tertiary = Color(0xFFD0BCFF), // Downloaded badge
        onTertiary = Color(0xFF381E72),
        tertiaryContainer = Color(0xFF4F378B),
        onTertiaryContainer = Color(0xFFEADDFF),
        background = Color(0xFF202124),
        onBackground = Color(0xFFE3E6E5),
        surface = Color(0xFF202124),
        onSurface = Color(0xFFE3E6E5),
        surfaceVariant = Color(0xFF2A2D2F),
        onSurfaceVariant = Color(0xFFC4CCCA),
        surfaceTint = Color(0xFF52D9CA),
        inverseSurface = Color(0xFFE3E2E6),
        inverseOnSurface = Color(0xFF1B1B1F),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        outline = Color(0xFF8F9099),
        outlineVariant = Color(0xFF44464F),
        surfaceContainerLowest = Color(0xFF1C1E20),
        surfaceContainerLow = Color(0xFF25282A),
        surfaceContainer = Color(0xFF2A2D2F),
        surfaceContainerHigh = Color(0xFF303436),
        surfaceContainerHighest = Color(0xFF383D3F),
    )

    override val lightScheme = lightColorScheme(
        primary = Color(0xFF006B60),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFF9EF2E6),
        onPrimaryContainer = Color(0xFF00201B),
        inversePrimary = Color(0xFF52D9CA),
        secondary = Color(0xFF006B60), // Unread badge
        onSecondary = Color(0xFFFFFFFF), // Unread badge text
        secondaryContainer = Color(0xFF9EF2E6),
        onSecondaryContainer = Color(0xFF00201B),
        tertiary = Color(0xFF6750A4), // Downloaded badge
        onTertiary = Color(0xFFFFFFFF), // Downloaded badge text
        tertiaryContainer = Color(0xFFEADDFF),
        onTertiaryContainer = Color(0xFF21005D),
        background = Color(0xFFFEFBFF),
        onBackground = Color(0xFF1B1B1F),
        surface = Color(0xFFFEFBFF),
        onSurface = Color(0xFF1B1B1F),
        surfaceVariant = Color(0xFFF3EDF7), // Navigation bar background (ThemePrefWidget)
        onSurfaceVariant = Color(0xFF44464F),
        surfaceTint = Color(0xFF006B60),
        inverseSurface = Color(0xFF303034),
        inverseOnSurface = Color(0xFFF2F0F4),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        outline = Color(0xFF757780),
        outlineVariant = Color(0xFFC5C6D0),
        surfaceContainerLowest = Color(0xFFF5F1F8),
        surfaceContainerLow = Color(0xFFF7F2FA),
        surfaceContainer = Color(0xFFF3EDF7), // Navigation bar background
        surfaceContainerHigh = Color(0xFFFCF7FF),
        surfaceContainerHighest = Color(0xFFFCF7FF),
    )
}
