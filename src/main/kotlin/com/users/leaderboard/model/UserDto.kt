package com.users.leaderboard.model

data class UserDto(
    val nickName: String,
    val point: Int = 0,
    var uniqueId: String = "",
    var token: String = ""
)
