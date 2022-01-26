package com.users.leaderboard.security.jwt

import com.users.leaderboard.common.Constants.KEY_SECRET
import com.users.leaderboard.common.Constants.ROLE
import com.users.leaderboard.common.Constants.ROLE_USER
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*
import java.util.function.Function
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter

@Service
class JwtUtil {
    val signatureAlgorithm = SignatureAlgorithm.HS512
    var userRole = ROLE_USER

    // getUsernameW/Token
    fun extractUsername(token: String?): String {
        return extractClaim(token, Claims::getSubject)
    }

    // getTokenExpirationDate
    fun extractExpiration(token: String?): Date {
        return extractClaim(token, Claims::getExpiration)
    }

    fun getRoleFromToken(token: String?): String {
        return Jwts.parser().setSigningKey(KEY_SECRET).parseClaimsJws(token).body[ROLE].toString()
    }

    fun getRole(): String{
        return userRole
    }

    fun <T> extractClaim(token: String?, claimsResolver: Function<Claims, T>): T {
        val claims = extractAllClaims(token)
        return claimsResolver.apply(claims)
    }

    // getClaimsW/Token
    private fun extractAllClaims(token: String?): Claims {
        return Jwts.parser().setSigningKey(KEY_SECRET).parseClaimsJws(token).body
    }

    private fun isTokenExpired(token: String?): Boolean {
        return extractExpiration(token).before(Date())
    }

    fun generateToken(user: UserDetails,role: String): String? {
        val claims: Map<String, Objects> = HashMap()
        return createToken(claims, user,role)
    }

    private fun createToken(claims: Map<String, Objects>, user: UserDetails, role: String): String? {
        val apiKeySecretBytes: ByteArray = DatatypeConverter.parseBase64Binary(KEY_SECRET)
        val signingKey: Key = SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.jcaName)

        return Jwts.builder().setClaims(claims)
            .claim(ROLE, role)
            .setSubject(user.username)
            .setId(user.password)
            .setIssuedAt(Date(System.currentTimeMillis())) // tokenStartedDate
            .setExpiration(Date(System.currentTimeMillis() + 3333333333333333 * 1000)) // tokenExpirationDate
            .signWith(signatureAlgorithm, signingKey) // sign with algorithm type and secret key
            .compact()
    }

    // token hala geçerli mi? kullanıcı adı doğru ise ve token ın geçerlilik süresi devam ediyorsa true döner.
    fun validateToken(token: String?, userDetails: UserDetails): Boolean? {
        val username = extractUsername(token)

        return if (username == userDetails.username && !isTokenExpired(token)) {
            userRole = getRoleFromToken(token)
            true
        } else {
            false
        }
    }
}