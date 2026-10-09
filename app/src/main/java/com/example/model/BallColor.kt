package com.example.model

import androidx.compose.ui.graphics.Color

enum class BallColor(
    val id: Int,
    val primaryColor: Color,
    val lightColor: Color,
    val darkColor: Color,
    val symbol: String,
    val symbolColor: Color
) {
    RUBY_RED(
        id = 0,
        primaryColor = Color(0xFFE53935),
        lightColor = Color(0xFFFF8A80),
        darkColor = Color(0xFFB71C1C),
        symbol = "★",
        symbolColor = Color.White
    ),
    SKY_BLUE(
        id = 1,
        primaryColor = Color(0xFF0288D1),
        lightColor = Color(0xFF81D4FA),
        darkColor = Color(0xFF01579B),
        symbol = "◆",
        symbolColor = Color.White
    ),
    EMERALD_GREEN(
        id = 2,
        primaryColor = Color(0xFF2E7D32),
        lightColor = Color(0xFFA5D6A7),
        darkColor = Color(0xFF1B5E20),
        symbol = "▲",
        symbolColor = Color.White
    ),
    SOLAR_GOLD(
        id = 3,
        primaryColor = Color(0xFFFFB300),
        lightColor = Color(0xFFFFE082),
        darkColor = Color(0xFFFF8F00),
        symbol = "●",
        symbolColor = Color(0xFF3E2723)
    ),
    AMETHYST_PURPLE(
        id = 4,
        primaryColor = Color(0xFF8E24AA),
        lightColor = Color(0xFFCE93D8),
        darkColor = Color(0xFF4A148C),
        symbol = "♥",
        symbolColor = Color.White
    ),
    SUNSET_ORANGE(
        id = 5,
        primaryColor = Color(0xFFF4511E),
        lightColor = Color(0xFFFFAB91),
        darkColor = Color(0xFFBF360C),
        symbol = "■",
        symbolColor = Color.White
    ),
    CYAN_TEAL(
        id = 6,
        primaryColor = Color(0xFF00ACC1),
        lightColor = Color(0xFF80DEEA),
        darkColor = Color(0xFF006064),
        symbol = "⬢",
        symbolColor = Color.White
    ),
    HOT_PINK(
        id = 7,
        primaryColor = Color(0xFFD81B60),
        lightColor = Color(0xFFF48FB1),
        darkColor = Color(0xFF880E4F),
        symbol = "✿",
        symbolColor = Color.White
    ),
    LIME_GREEN(
        id = 8,
        primaryColor = Color(0xFF7CB342),
        lightColor = Color(0xFFC5E1A5),
        darkColor = Color(0xFF33691E),
        symbol = "♣",
        symbolColor = Color.White
    ),
    DEEP_INDIGO(
        id = 9,
        primaryColor = Color(0xFF3949AB),
        lightColor = Color(0xFF9FA8DA),
        darkColor = Color(0xFF1A237E),
        symbol = "☀",
        symbolColor = Color.White
    ),
    MINT_AQUA(
        id = 10,
        primaryColor = Color(0xFF00897B),
        lightColor = Color(0xFF80CBC4),
        darkColor = Color(0xFF004D40),
        symbol = "☁",
        symbolColor = Color.White
    ),
    EARTH_BROWN(
        id = 11,
        primaryColor = Color(0xFF8D6E63),
        lightColor = Color(0xFFD7CCC8),
        darkColor = Color(0xFF4E342E),
        symbol = "⚡",
        symbolColor = Color.White
    );

    companion object {
        fun fromId(id: Int): BallColor = entries.firstOrNull { it.id == id } ?: RUBY_RED
    }
}
