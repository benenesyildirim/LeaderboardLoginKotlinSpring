package com.users.leaderboard.firebase

import com.google.cloud.firestore.*
import com.google.firebase.cloud.FirestoreClient
import com.users.leaderboard.common.Constants.ADMIN_TOKEN
import com.users.leaderboard.model.UserDto
import org.springframework.stereotype.Service
import java.util.*

@Service
class FirebaseService {
    val fireStore: Firestore = FirestoreClient.getFirestore()
    var users = mutableListOf<UserDto>()

    fun createUser(user: UserDto): String {
        user.uniqueId = createUniqueId()
        val collection = fireStore.collection("users").document(user.nickName).set(user)
        return if (collection.get().updateTime.toString().isNotEmpty())
            "OK"
        else
            "Problem"
    }

    fun updateUser(user: UserDto): String {
        users = getAllUsers(ADMIN_TOKEN,true)

        for (tempUser in users){
            if (tempUser.nickName == user.nickName){
                user.token = tempUser.token
            }
        }

        user.uniqueId = getUniqueId(user.nickName,users)
        val collection = fireStore.collection("users").document(user.nickName).set(user)
        return collection.get().updateTime.toString()
    }

    fun deleteUser(username: String): String {
        fireStore.collection("users").document(username).delete()
        return "$username deleted."
    }

    fun getUser(username: String): UserDto? {
        val documentReference: DocumentReference = fireStore.collection("users").document(username)
        val future = documentReference.get()
        val document = future.get()
        if (document.exists()) {
            return UserDto(
                document.get("nickName").toString(),
                Integer.valueOf(document.get("point").toString())
            )
        }
        return null
    }

    fun getLeaderBoard(token: String): MutableList<UserDto> {
        users = getAllUsers(ADMIN_TOKEN,true)
        val leaderBoardList = mutableListOf<UserDto>()
        for (user in users){
            user.uniqueId = ""
            leaderBoardList.add(user)
        }
        leaderBoardList.sortByDescending { it.point }
        return users
    }

    fun getAllUsers(token: String, isAdmin: Boolean): MutableList<UserDto> {
        val documents = fireStore.collection("users").get().get().documents
        val users = mutableListOf<UserDto>()
        for (i in documents) {
            users.add(toUser(i, isAdmin))
        }
        return users
    }

    fun toUser(query: QueryDocumentSnapshot, isAdmin: Boolean): UserDto {
        return if (isAdmin)
            UserDto(
                query.data["nickName"].toString(),
                Integer.parseInt(query.data["point"].toString()),
                query.data["uniqueId"].toString(),
                query.data["token"].toString()
            )
        else UserDto(
            query.data["nickName"].toString(),
            Integer.parseInt(query.data["point"].toString())
        )
    }

    private fun createUniqueId(): String{
        users = getAllUsers(ADMIN_TOKEN,true)
        var random = UUID.randomUUID().toString()
        for (user in users){
            if (user.uniqueId == random){
                random = UUID.randomUUID().toString()
            }
        }
        return random
    }

    fun getUniqueId(nickName: String, users: MutableList<UserDto>): String{
        for (user in users){
            if (user.nickName == nickName) return user.uniqueId
        }
        return createUniqueId()
    }

    fun isNickNameUsable(nickName: String): Boolean{
        users = getAllUsers(ADMIN_TOKEN,true)
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