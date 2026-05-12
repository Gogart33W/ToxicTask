package com.example.toxictask

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class Priority(val label: String, val color: Color, val icon: ImageVector, val weight: Int) {
    LOW("LOW", Color(0xFF34C759), Icons.Rounded.ArrowDownward, 1),
    MEDIUM("MID", Color(0xFFFFCC00), Icons.Rounded.Remove, 3),
    HARD("HARDCORE", Color(0xFFFF3B30), Icons.Rounded.Whatshot, 6)
}
