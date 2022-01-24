package com.users.leaderboard.security

import com.users.leaderboard.model.UserDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import kotlin.jvm.Throws

@Service
class MyUserDetailService: UserDetailsService{

    @Autowired
    private lateinit var userService: UserService

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String?): UserDetails {
        return userService.getUserByUsername(username!!)
    }

    fun addToUsers(user: UserDto){
        userService.addToUsers(user)
    }
}