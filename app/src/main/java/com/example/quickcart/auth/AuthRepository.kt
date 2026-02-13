package com.example.quickcart.auth

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AuthRepository(
    private val firebaseAuthProvider: () -> FirebaseAuth = { FirebaseAuth.getInstance() }
) {
    private fun auth(): FirebaseAuth = firebaseAuthProvider()

    fun observeUserId(): Flow<String?> = callbackFlow {
        val firebaseAuth = auth()
        val listener = FirebaseAuth.AuthStateListener { auth -> trySend(auth.currentUser?.uid) }
        firebaseAuth.addAuthStateListener(listener)
        trySend(firebaseAuth.currentUser?.uid)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    fun sendOtp(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: (verificationId: String, token: PhoneAuthProvider.ForceResendingToken) -> Unit,
        onVerified: (PhoneAuthCredential) -> Unit,
        onFailure: (FirebaseException) -> Unit
    ) {
        val firebaseAuth = auth()
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                onVerified(credential)
            }

            override fun onVerificationFailed(error: FirebaseException) {
                onFailure(error)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                onCodeSent(verificationId, token)
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun signInWithCredential(credential: PhoneAuthCredential) {
        auth().signInWithCredential(credential).await()
    }

    suspend fun verifyOtpAndSignIn(verificationId: String, otp: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        signInWithCredential(credential)
    }

    suspend fun signInWithEmailPassword(email: String, password: String): String {
        val result = auth().signInWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw IllegalStateException("User id missing after sign in")
    }

    fun signOut() {
        auth().signOut()
    }
}
