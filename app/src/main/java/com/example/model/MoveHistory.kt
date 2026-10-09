package com.example.model

data class MoveHistory(
    val tubes: List<Tube>,
    val fromTubeIndex: Int,
    val toTubeIndex: Int
)
