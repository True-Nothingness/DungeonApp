package com.light.dungeonofhabits.models

data class BattleResponse(
    val message: String,
    val xp: Int,
    val gold: Int,
    val remainingHP: Int,
    val highestFloorReached: Int,
    val levelUp: Boolean
)
