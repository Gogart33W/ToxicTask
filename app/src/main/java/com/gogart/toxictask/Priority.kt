package com.gogart.toxictask

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class Priority(val weight: Int, val color: Color, val icon: ImageVector) {
    LOW(1, Color.Green, Icons.Default.KeyboardArrowDown),
    MEDIUM(2, Color.Yellow, Icons.Default.Bolt),
    HARD(4, Color.Red, Icons.Default.Whatshot)
}
