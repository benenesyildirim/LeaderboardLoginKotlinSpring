package com.users.leaderboard.model

data class RequestAuth(
    // TODO sonradan val yapabiliriz.
    var username: String,
    var uniqueId: String
)