package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * ─────────────────────────────────────────────────────────────────────────────
 *  nayaMDCAT — Premium Design System · Color Tokens
 * ─────────────────────────────────────────────────────────────────────────────
 *  A cohesive Material 3 palette tuned for a modern, premium exam-prep app.
 *  Brand: confident Royal Blue (trust / focus), a warm Amber accent (energy),
 *  and a fresh Teal tertiary (progress), grounded on refined Slate neutrals.
 *  Values stay within the Tailwind color family already used across the UI so
 *  existing hard-coded accents remain visually harmonious.
 * ─────────────────────────────────────────────────────────────────────────────
 */

// ── Light · Primary (Royal Blue) ────────────────────────────────────────────
val HighDensityPrimary = Color(0xFF2563EB)          // Blue 600 — brand primary
val HighDensityOnPrimary = Color(0xFFFFFFFF)
val HighDensityPrimaryContainer = Color(0xFFDBEAFE) // Blue 100 — soft wash
val HighDensityOnPrimaryContainer = Color(0xFF17275C) // Deep navy for contrast

// ── Light · Secondary (Amber accent) ────────────────────────────────────────
val HighDensitySecondary = Color(0xFFF59E0B)        // Amber 500 — warm accent
val HighDensityOnSecondary = Color(0xFFFFFFFF)
val HighDensitySecondaryContainer = Color(0xFFFEF3C7) // Amber 100
val HighDensityOnSecondaryContainer = Color(0xFF7C2D12) // Deep rust text

// ── Light · Tertiary (Teal / progress) ──────────────────────────────────────
val HighDensityTertiary = Color(0xFF0D9488)         // Teal 600
val HighDensityOnTertiary = Color(0xFFFFFFFF)
val HighDensityTertiaryContainer = Color(0xFFCCFBF1) // Teal 100
val HighDensityOnTertiaryContainer = Color(0xFF134E4A)

// ── Light · Error ────────────────────────────────────────────────────────────
val HighDensityError = Color(0xFFDC2626)            // Red 600
val HighDensityOnError = Color(0xFFFFFFFF)
val HighDensityErrorContainer = Color(0xFFFEE2E2)   // Red 100
val HighDensityOnErrorContainer = Color(0xFF7F1D1D)

// ── Light · Neutrals & surfaces (Slate) ──────────────────────────────────────
val HighDensityBackground = Color(0xFFF8FAFC)       // Slate 50
val HighDensityOnBackground = Color(0xFF0F172A)     // Slate 900
val HighDensitySurface = Color(0xFFFFFFFF)          // Pure white
val HighDensityOnSurface = Color(0xFF0F172A)
val HighDensitySurfaceVariant = Color(0xFFE2E8F0)   // Slate 200
val HighDensityOnSurfaceVariant = Color(0xFF475569) // Slate 600 — supporting text
val HighDensityOutline = Color(0xFFCBD5E1)          // Slate 300 — borders
val HighDensityOutlineVariant = Color(0xFFE2E8F0)   // Slate 200 — hairlines

// M3 surface-container elevation ramp (light)
val HighDensitySurfaceDim = Color(0xFFE7ECF3)
val HighDensitySurfaceBright = Color(0xFFFFFFFF)
val HighDensitySurfaceContainerLowest = Color(0xFFFFFFFF)
val HighDensitySurfaceContainerLow = Color(0xFFF8FAFC)
val HighDensitySurfaceContainer = Color(0xFFF1F5F9)
val HighDensitySurfaceContainerHigh = Color(0xFFE9EEF5)
val HighDensitySurfaceContainerHighest = Color(0xFFE2E8F0)

// Inverse / utility (light)
val HighDensityInverseSurface = Color(0xFF1E293B)
val HighDensityInverseOnSurface = Color(0xFFF1F5F9)
val HighDensityInversePrimary = Color(0xFF93C5FD)
val HighDensityScrim = Color(0xFF000000)

// ── Dark · Primary ───────────────────────────────────────────────────────────
val HighDensityDarkPrimary = Color(0xFF93C5FD)       // Blue 300 — glowing accent
val HighDensityDarkOnPrimary = Color(0xFF10275C)
val HighDensityDarkPrimaryContainer = Color(0xFF1E40AF) // Blue 800 void
val HighDensityDarkOnPrimaryContainer = Color(0xFFDBEAFE)

// ── Dark · Secondary ─────────────────────────────────────────────────────────
val HighDensityDarkSecondary = Color(0xFFFBBF24)     // Amber 400
val HighDensityDarkOnSecondary = Color(0xFF422006)
val HighDensityDarkSecondaryContainer = Color(0xFF92400E) // Amber 800
val HighDensityDarkOnSecondaryContainer = Color(0xFFFEF3C7)

// ── Dark · Tertiary ──────────────────────────────────────────────────────────
val HighDensityDarkTertiary = Color(0xFF5EEAD4)      // Teal 300
val HighDensityDarkOnTertiary = Color(0xFF0A3B36)
val HighDensityDarkTertiaryContainer = Color(0xFF115E59) // Teal 800
val HighDensityDarkOnTertiaryContainer = Color(0xFFCCFBF1)

// ── Dark · Error ─────────────────────────────────────────────────────────────
val HighDensityDarkError = Color(0xFFFCA5A5)
val HighDensityDarkOnError = Color(0xFF450A0A)
val HighDensityDarkErrorContainer = Color(0xFF991B1B)
val HighDensityDarkOnErrorContainer = Color(0xFFFEE2E2)

// ── Dark · Neutrals & surfaces ───────────────────────────────────────────────
val HighDensityDarkBackground = Color(0xFF0B1120)    // Deep navy slate
val HighDensityDarkOnBackground = Color(0xFFE5E9F0)
val HighDensityDarkSurface = Color(0xFF111827)       // Slate 900 card base
val HighDensityDarkOnSurface = Color(0xFFE5E9F0)
val HighDensityDarkSurfaceVariant = Color(0xFF334155) // Slate 700
val HighDensityDarkOnSurfaceVariant = Color(0xFFA9B4C4)
val HighDensityDarkOutline = Color(0xFF475569)        // Slate 600
val HighDensityDarkOutlineVariant = Color(0xFF334155)

// M3 surface-container elevation ramp (dark)
val HighDensityDarkSurfaceDim = Color(0xFF0B1120)
val HighDensityDarkSurfaceBright = Color(0xFF29344A)
val HighDensityDarkSurfaceContainerLowest = Color(0xFF070C16)
val HighDensityDarkSurfaceContainerLow = Color(0xFF111827)
val HighDensityDarkSurfaceContainer = Color(0xFF161E2E)
val HighDensityDarkSurfaceContainerHigh = Color(0xFF1F2937)
val HighDensityDarkSurfaceContainerHighest = Color(0xFF29303F)

// Inverse / utility (dark)
val HighDensityDarkInverseSurface = Color(0xFFE5E9F0)
val HighDensityDarkInverseOnSurface = Color(0xFF1E293B)
val HighDensityDarkInversePrimary = Color(0xFF2563EB)
val HighDensityDarkScrim = Color(0xFF000000)

/**
 * Semantic accent tokens for status-style UI (success / warning / info) that
 * live outside the Material color roles. Exposed for incremental adoption so
 * screens can migrate ad-hoc greens/oranges to a single source of truth.
 */
object BrandColors {
    // Light
    val Success = Color(0xFF16A34A)
    val OnSuccess = Color(0xFFFFFFFF)
    val SuccessContainer = Color(0xFFDCFCE7)
    val OnSuccessContainer = Color(0xFF14532D)

    val Warning = Color(0xFFD97706)
    val OnWarning = Color(0xFFFFFFFF)
    val WarningContainer = Color(0xFFFEF3C7)
    val OnWarningContainer = Color(0xFF78350F)

    val Info = Color(0xFF0284C7)
    val OnInfo = Color(0xFFFFFFFF)
    val InfoContainer = Color(0xFFE0F2FE)
    val OnInfoContainer = Color(0xFF075985)

    // Dark
    val SuccessDark = Color(0xFF4ADE80)
    val SuccessContainerDark = Color(0xFF166534)
    val WarningDark = Color(0xFFFBBF24)
    val WarningContainerDark = Color(0xFF92400E)
    val InfoDark = Color(0xFF38BDF8)
    val InfoContainerDark = Color(0xFF075985)
}
