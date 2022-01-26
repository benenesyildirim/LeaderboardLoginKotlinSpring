package com.users.leaderboard.security

import com.users.leaderboard.firebase.FirebaseService
import com.users.leaderboard.model.UserDto
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class UserService {
    private val users: MutableMap<String, User> = HashMap()

    @PostConstruct
    fun initialize() {
        val users = FirebaseService().getAllUsers(true)

        this.users["Admin"] = User("Admin", "12345", listOf())
        for (user in users) {
            this.users[user.nickName] = User(user.nickName, user.uniqueId, listOf())
        }
    }

    fun getUserByUsername(username: String): User {
        return users[username]!!
    }

    fun addToUsers(user: UserDto) {
        this.users[user.nickName] = User(user.nickName, user.uniqueId, listOf())
    }
}