package com.users.leaderboard.rest

import com.users.leaderboard.common.Constants
import com.users.leaderboard.common.Constants.ROLE_ADMIN
import com.users.leaderboard.common.Constants.ROLE_USER
import com.users.leaderboard.firebase.FirebaseService
import com.users.leaderboard.model.RequestAuth
import com.users.leaderboard.model.UserDto
import com.users.leaderboard.security.MyUserDetailService
import com.users.leaderboard.security.jwt.JwtUtil
import org.apache.juli.logging.Log
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
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
    private lateinit var userDetailService: MyUserDetailService

    @GetMapping("check-nickname")
    fun isNickNameUsable(nickName: String, uniqueId: String?, role: String?): ResponseEntity<String> {
        val auth = RequestAuth(nickName, uniqueId ?: "")

        return if (firebaseService.isNickNameUsable(nickName)) {
            val newUser = UserDto(nickName = auth.username)
            userDetailService.addToUsers(newUser)
            FirebaseService().createUser(newUser)
            ResponseEntity.ok(createToken(auth, role ?: ROLE_USER))
        } else {
            ResponseEntity.badRequest().body("Please try again, nickname already using in database.")
        }
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    @GetMapping("/get-user")
    fun getUser(@RequestParam username: String): ResponseEntity<UserDto> {
        return if (firebaseService.getUser(username) != null)
            ResponseEntity.ok()
                .header("message", "success")
                .body(firebaseService.getUser(username))
        else
            ResponseEntity.badRequest().header("message", "User can not found").body(null)
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    @GetMapping("/get-all")
    fun getAllUsers(@RequestParam token: String): ResponseEntity<MutableList<UserDto>> {
        return if (jwtUtil.getRole() == ROLE_ADMIN) {
            ResponseEntity.ok()
                .header("message", "success")
                .body(firebaseService.getAllUsers(token, true))
        } else
            ResponseEntity.badRequest().header("message", "You are not admin you can not get all users.").body(null)
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    @GetMapping("/get-leaderboard")
    fun getLeaderboard(@RequestParam token: String): ResponseEntity<MutableList<UserDto>> {
        return if (firebaseService.getLeaderBoard(token).size > 0){
            ResponseEntity.ok()
                .header("message", "success")
                .body(firebaseService.getLeaderBoard(token))
        }else ResponseEntity.badRequest().header("message", "There is no users.").body(null)
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
            return "Incorrect username or password"
        }

        userDetails = userDetailService.loadUserByUsername(authRequest.username)
        val token = jwtUtil.generateToken(userDetails, role)!!

        firebaseService.setTokenToUser(token, UserDto(authRequest.username))
        return token
    }

    // Firebase C.R.U.D.
    @Throws(InterruptedException::class, ExecutionException::class)
    @PostMapping("/create-user")
    fun createUser(@RequestBody user: UserDto): String {
        return firebaseService.createUser(user)
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    @PutMapping("/update-user")
    fun updateUser(@RequestBody user: UserDto): String {
        return firebaseService.updateUser(user)
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    @PutMapping("/delete-user")
    fun deleteUser(@RequestParam username: String): String {
        return firebaseService.deleteUser(username)
    }

    @GetMapping("/mobiltest")
    fun testMobil(): String{
        LoggerFactory.getLogger(this.javaClass).info("AAAAAAAAAAAAAAAAAAAAAA")
        return "Oleey!"

    }
}