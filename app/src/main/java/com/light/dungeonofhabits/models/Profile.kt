package com.light.dungeonofhabits.models


data class Profile(
    val user: User,
    val petLeft: Boolean,
    val hpLost: Int,
    val resetOccurred: Boolean
)



