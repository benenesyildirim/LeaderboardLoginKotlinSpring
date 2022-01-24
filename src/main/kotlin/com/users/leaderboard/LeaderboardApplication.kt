package com.users.leaderboard

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.FileInputStream


@SpringBootApplication
class LeaderboardApplication

fun main(args: Array<String>) {

	val serviceAccount = FileInputStream("src/main/resources/firebaseLeaderboardAdminKey.json")

	val options = FirebaseOptions.builder()
		.setCredentials(GoogleCredentials.fromStream(serviceAccount))
		.build()

	FirebaseApp.initializeApp(options)

	runApplication<LeaderboardApplication>(*args)
}
