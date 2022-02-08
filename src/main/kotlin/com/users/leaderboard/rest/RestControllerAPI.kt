package com.users.leaderboard.rest

import com.users.leaderboard.common.Constants.CHECK_NICKNAME
import com.users.leaderboard.common.Constants.CREATE_USER
import com.users.leaderboard.common.Constants.DELETE_USER
import com.users.leaderboard.common.Constants.EMPTY_STRING
import com.users.leaderboard.common.Constants.GET_ALL
import com.users.leaderboard.common.Constants.GET_LEADERBOARD
import com.users.leaderboard.common.Constants.GET_USER
import com.users.leaderboard.common.Constants.INCORRECT_USER
import com.users.leaderboard.common.Constants.MESSAGE
import com.users.leaderboard.common.Constants.NO_USERS
import com.users.leaderboard.common.Constants.ROLE_ADMIN
import com.users.leaderboard.common.Constants.ROLE_USER
import com.users.leaderboard.common.Constants.SUCCESS
import com.users.leaderboard.common.Constants.UPDATE_USER
import com.users.leaderboard.common.Constants.USER_ALREADY_USING
import com.users.leaderboard.common.Constants.USER_CANT_FIND
import com.users.leaderboard.common.Constants.U_R_NOT_ADMIN
import com.users.leaderboard.firebase.FirebaseService
import com.users.leaderboard.model.RequestAuth
import com.users.leaderboard.model.UserDto
import com.users.leaderboard.security.UserService
import com.users.leaderboard.security.jwt.JwtUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.util.concurrent.ExecutionException

@RestController
class RestControllerAPI(val firebaseService: FirebaseService) {
    private lateinit var userDetails: UserDetails

    @Autowired
    private lateinit var jwtUtil: JwtUtil

    @Autowired
    private lateinit var authenticationManager: AuthenticationManager

    @Autowired
    private lateinit var userService: UserService

    @GetMapping(CHECK_NICKNAME)
    fun isNickNameUsable(nickName: String, uniqueId: String?, role: String?): ResponseEntity<String> {
        val auth = RequestAuth(nickName, uniqueId ?: EMPTY_STRING)

        return if (firebaseService.isNickNameUsable(nickName)) {
            userService.addToUsers(UserDto(nickName = auth.username))
            ResponseEntity.ok(createToken(auth, role ?: ROLE_USER))
        } else {
            ResponseEntity.badRequest().body(USER_ALREADY_USING)
        }
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    @GetMapping(GET_USER)
    fun getUser(@RequestParam username: String): ResponseEntity<UserDto> {
        val user = firebaseService.getUser(username)
        return if (user != null)
            ResponseEntity.ok()
                .header(MESSAGE, SUCCESS)
                .body(user)
        else
            ResponseEntity.badRequest().header(MESSAGE, USER_CANT_FIND).body(null)
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    @GetMapping(GET_ALL)
    fun getAllUsers(): ResponseEntity<MutableList<UserDto>> {
        return if (jwtUtil.getRole() == ROLE_ADMIN) {
            ResponseEntity.ok()
                .header(MESSAGE, SUCCESS)
                .body(firebaseService.getAllUsers(true))
        } else
            ResponseEntity.badRequest().header(MESSAGE, U_R_NOT_ADMIN).body(null)
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    @GetMapping(GET_LEADERBOARD)
    fun getLeaderboard(): ResponseEntity<MutableList<UserDto>> {
        return if (firebaseService.getLeaderBoard().size > 0){
            ResponseEntity.ok()
                .header(MESSAGE, SUCCESS)
                .body(firebaseService.getLeaderBoard())
        }else ResponseEntity.badRequest().header(MESSAGE, NO_USERS).body(null)
    }

    fun createToken(authRequest: RequestAuth, role: String = ROLE_USER): String {
        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    authRequest.username,
                    authRequest.uniqueId
                )
            )
        } catch (exception: BadCredentialsException) {
            return INCORRECT_USER
        }

        userDetails = userService.getUserByUsername(authRequest.username)
        FirebaseService().createUser(UserDto(nickName = authRequest.username))
        val token = jwtUtil.generateToken(userDetails, role)!!

        firebaseService.setTokenToUser(token, UserDto(authRequest.username))
        return token
    }

    // Firebase C.R.U.D.
    @Throws(InterruptedException::class, ExecutionException::class)
    @PostMapping(CREATE_USER)
    fun createUser(@RequestBody user: UserDto): String {
        return firebaseService.createUser(user)
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    @PutMapping(UPDATE_USER)
    fun updateUser(@RequestBody user: UserDto): String {
        return firebaseService.updateUser(user)
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    @PutMapping(DELETE_USER)
    fun deleteUser(@RequestParam username: String): String {
        return firebaseService.deleteUser(username)
    }
}