package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/*
 * ─────────────────────────────────────────────────────────────────────────────
 *  nayaMDCAT — Premium Design System · Shape Scale
 * ─────────────────────────────────────────────────────────────────────────────
 *  Softer, larger corner radii across the board for a calmer, more premium
 *  feel (cards read like content tiles, buttons feel tappable and friendly).
 * ─────────────────────────────────────────────────────────────────────────────
 */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
