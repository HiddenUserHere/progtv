package dev.jvfl.progtv.ui.theme

import androidx.compose.ui.graphics.Color

// Core palette — blue on near-black with translucent "glass" surfaces.
val BgBlack = Color(0xFF0A0E14) // app base / player letterbox
val BgSoft = Color(0xFF0F141C) // raised solid surface
val BgElevated = Color(0xFF141A24) // opaque row fill when needed

val BrandBlue = Color(0xFF3B82F6) // primary accent, focus fill tint base
val BrandSoft = Color(0xFF60A5FA) // accent text, subtitle "live"
val BrandDeep = Color(0xFF1D4ED8) // primary button rest

val TextPrimary = Color(0xFFE5E9F0) // titles / values
val TextMuted = Color(0xFF8B93A7) // subtitles / labels

val StarAmber = Color(0xFFFBBF24) // favorite star (promoted to a theme token)

// Glass tokens.
val GlassFill = Color(0xB30D1220) // panel fill — ~70% over #0D1220 for premium depth
val GlassFillStrong = Color(0xE60B0F1A) // rail / modal fill (~90%)
val GlassFillFocus = Color(0x243B82F6) // focused-row fill = BrandBlue @ 14%
val GlassStroke = Color(0x14FFFFFF) // hairline border = white 8%
val GlassStrokeSoft = Color(0x0AFFFFFF) // white 4%, inner separators
val FocusStroke = Color(0xFF60A5FA) // focused border
val FocusGlow = Color(0x663B82F6) // glow color = BrandBlue @ 40%

val Scrim = Color(0x99000000) // player dim
val ScrimStrong = Color(0xC7000000) // ~78% for modal backdrops

val ErrorRed = Color(0xFFEF4444) // error
val OnlineGreen = Color(0xFF34D399) // reserved / online dot
