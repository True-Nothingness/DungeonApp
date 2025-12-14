package com.light.dungeonofhabits.models

data class CharacterResponse(
    val characters: List<String>,
    val pets: List<String>,
    val stats: Map<String, Stat>
)

data class Stat(
    val atk: Int,
    val def: Int,
    val hp: Int
)
