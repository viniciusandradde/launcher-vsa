package com.viniciusandrade.kidslauncher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.viniciusandrade.kidslauncher.R

// Baloo 2 — a rounded, friendly variable font bundled in res/font. Each weight is
// pulled from the single variable file via FontVariation (API 26+).
@OptIn(ExperimentalTextApi::class)
private fun baloo(weight: Int) = Font(
    resId = R.font.baloo2,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val BalooFamily = FontFamily(
    baloo(400),
    baloo(500),
    baloo(600),
    baloo(700),
    baloo(800),
)

// Larger-than-default, rounded type: young children benefit from bigger, bolder text.
val KidsTypography = Typography(
    displayLarge = TextStyle(fontFamily = BalooFamily, fontWeight = FontWeight.Black, fontSize = 44.sp, lineHeight = 52.sp),
    headlineMedium = TextStyle(fontFamily = BalooFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp),
    titleLarge = TextStyle(fontFamily = BalooFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    bodyLarge = TextStyle(fontFamily = BalooFamily, fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 26.sp),
    labelLarge = TextStyle(fontFamily = BalooFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
)
