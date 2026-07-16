package com.viniciusandrade.kidslauncher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Larger-than-default type: young children benefit from bigger, bolder text.
val KidsTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Black, fontSize = 44.sp, lineHeight = 52.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 26.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
)
