package com.songajae.amgi.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object FirebaseServiceProvider {
    fun authOrNull(): FirebaseAuth? = try {
        Firebase.auth
    } catch (_: IllegalStateException) {
        null
    }

    fun firestoreOrNull(): FirebaseFirestore? = try {
        Firebase.firestore
    } catch (_: IllegalStateException) {
        null
    }
}