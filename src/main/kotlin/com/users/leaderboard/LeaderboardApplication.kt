package com.users.leaderboard

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.users.leaderboard.common.Constants.FIRESTORE_PATH
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.FileInputStream


@SpringBootApplication
class LeaderboardApplication

fun main(args: Array<String>) {

	initFirebase()
	runApplication<LeaderboardApplication>(*args)
}

private fun initFirebase() {
	val serviceAccount = FileInputStream(FIRESTORE_PATH)

	val options = FirebaseOptions.builder()
		.setCredentials(GoogleCredentials.fromStream(serviceAccount))
		.build()

	FirebaseApp.initializeApp(options)
}
