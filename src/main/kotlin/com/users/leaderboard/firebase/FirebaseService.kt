package com.users.leaderboard.firebase

import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.QueryDocumentSnapshot
import com.google.firebase.cloud.FirestoreClient
import com.users.leaderboard.common.Constants.EMPTY_STRING
import com.users.leaderboard.common.Constants.NICKNAME
import com.users.leaderboard.common.Constants.POINT
import com.users.leaderboard.common.Constants.TOKEN
import com.users.leaderboard.common.Constants.UNIQUEID
import com.users.leaderboard.common.Constants.USER_COLLECTION
import com.users.leaderboard.model.UserDto
import org.springframework.stereotype.Service
import java.util.*

@Service
class FirebaseService {
    val fireStore: Firestore = FirestoreClient.getFirestore()
    var users = mutableListOf<UserDto>()

    fun createUser(user: UserDto): String {
        user.uniqueId = createUniqueId()
        val userDocument = fireStore.collection(USER_COLLECTION).document(user.nickName).set(user)
        return if (userDocument.get().updateTime.toString().isNotEmpty())
            "OK"
        else
            "Problem"
    }

    fun updateUser(user: UserDto): String {
        users = getAllUsers(true)

        user.uniqueId = getUniqueId(user.nickName,users)
        val userDocument = fireStore.collection(USER_COLLECTION).document(user.nickName).set(user)
        return userDocument.get().updateTime.toString()
    }

    fun deleteUser(username: String): String {
        fireStore.collection(USER_COLLECTION).document(username).delete()
        return "$username deleted."
    }

    fun getUser(username: String): UserDto? {
        val userDocumentReference: DocumentReference = fireStore.collection(USER_COLLECTION).document(username)
        val userDocument = userDocumentReference.get()
        val document = userDocument.get()
        if (document.exists()) {
            return UserDto(
                document.get(NICKNAME).toString(),
                Integer.valueOf(document.get(POINT).toString())
            )
        }
        return null
    }

    fun getLeaderBoard(): MutableList<UserDto> {
        users = getAllUsers(false)
        val leaderBoardList = mutableListOf<UserDto>()
        for (user in users){
            user.uniqueId = EMPTY_STRING
            leaderBoardList.add(user)
        }
        leaderBoardList.sortByDescending { it.point }
        return leaderBoardList
    }

    fun getAllUsers(isAdmin: Boolean): MutableList<UserDto> {
        val documents = fireStore.collection(USER_COLLECTION).get().get().documents
        val users = mutableListOf<UserDto>()
        for (i in documents) {
            users.add(toUser(i, isAdmin))
        }
        return users
    }

    fun toUser(query: QueryDocumentSnapshot, isAdmin: Boolean): UserDto {
        return if (isAdmin)
            UserDto(
                query.data[NICKNAME].toString(),
                Integer.parseInt(query.data[POINT].toString()),
                query.data[UNIQUEID].toString(),
                query.data[TOKEN].toString()
            )
        else UserDto(
            query.data[NICKNAME].toString(),
            Integer.parseInt(query.data[POINT].toString())
        )
    }

    private fun createUniqueId(): String{
        users = getAllUsers(true)
        var randomUniqueId = UUID.randomUUID().toString()
        for (user in users){
            if (user.uniqueId == randomUniqueId){
                randomUniqueId = UUID.randomUUID().toString()
            }
        }
        return randomUniqueId
    }

    fun getUniqueId(nickName: String, users: MutableList<UserDto>): String{
        for (user in users){
            if (user.nickName == nickName) return user.uniqueId
        }
        return createUniqueId()
    }

    fun isNickNameUsable(nickName: String): Boolean{
        users = getAllUsers(true)

        for (user in users){
            if (user.nickName == nickName){
                return false
            }
        }
        return true
    }

    fun setTokenToUser(token: String,user: UserDto){
        user.token = token
        updateUser(user)
    }
}