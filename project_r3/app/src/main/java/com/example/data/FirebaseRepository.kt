package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirebaseRepository(private val context: Context) {

    private val db: FirebaseFirestore by lazy {
        if (FirebaseApp.getApps(context).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApiKey("AIzaSyBmUJGFkIPMyN2tOVk3LFe4u7ZLky6CKkQ")
                .setApplicationId("Loop.live")
                .setProjectId("loop-7e3d9")
                .build()
            FirebaseApp.initializeApp(context, options)
            Log.d("FirebaseRepository", "Dynamic FirebaseApp initialized for project: loop-7e3d9")
        }
        FirebaseFirestore.getInstance()
    }

    suspend fun fetchCredentials(code: String): Triple<String, String, String>? = suspendCancellableCoroutine { continuation ->
        try {
            db.collection("activation_codes").document(code).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val host = document.getString("host") ?: ""
                        val username = document.getString("username") ?: ""
                        val password = document.getString("password") ?: ""
                        
                        Log.d("FirebaseRepository", "Found credentials for code: $code")
                        if (host.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                            continuation.resume(Triple(host, username, password))
                        } else {
                            continuation.resume(null)
                        }
                    } else {
                        Log.w("FirebaseRepository", "No collection document matches code: $code")
                        continuation.resume(null)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FirebaseRepository", "FirebaseFirestore retrieval error: ${exception.message}", exception)
                    continuation.resume(null)
                }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Fatal exception in FirebaseRepository: ${e.message}", e)
            continuation.resume(null)
        }
    }
}
