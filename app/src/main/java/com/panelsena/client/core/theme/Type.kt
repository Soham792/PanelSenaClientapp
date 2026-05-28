package com.panelsena.client.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.panelsena.client.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val SyneFontName = GoogleFont("Syne")
val SyneFontFamily = FontFamily(
    Font(googleFont = SyneFontName, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = SyneFontName, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = SyneFontName, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = SyneFontName, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = SyneFontName, fontProvider = provider, weight = FontWeight.ExtraBold)
)

val DmSansFontName = GoogleFont("DM Sans")
val DmSansFontFamily = FontFamily(
    Font(googleFont = DmSansFontName, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = DmSansFontName, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = DmSansFontName, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = DmSansFontName, fontProvider = provider, weight = FontWeight.Bold)
)

val PanelSenaClientTypography = Typography(
    displayLarge  = TextStyle(fontFamily = SyneFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 40.sp),
    headlineLarge = TextStyle(fontFamily = SyneFontFamily, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
    headlineMedium= TextStyle(fontFamily = SyneFontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge    = TextStyle(fontFamily = SyneFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleMedium   = TextStyle(fontFamily = DmSansFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge     = TextStyle(fontFamily = DmSansFontFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp),
    bodyMedium    = TextStyle(fontFamily = DmSansFontFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp),
    labelLarge    = TextStyle(fontFamily = DmSansFontFamily, fontWeight = FontWeight.Medium, fontSize = 13.sp),
    labelSmall    = TextStyle(fontFamily = DmSansFontFamily, fontWeight = FontWeight.Normal, fontSize = 11.sp),
)
