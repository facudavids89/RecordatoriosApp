
package com.example.recordatoriosapp.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    val usuarioActual: FirebaseUser?
        get() = firebaseAuth.currentUser

    suspend fun registrar(
        email: String,
        password: String
    ): FirebaseUser {
        val resultado = firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .await()

        return resultado.user
            ?: throw Exception("No se pudo obtener el usuario registrado")
    }

    suspend fun iniciarSesion(
        email: String,
        password: String
    ): FirebaseUser {
        val resultado = firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .await()

        return resultado.user
            ?: throw Exception("No se pudo iniciar sesión")
    }

    fun cerrarSesion() {
        firebaseAuth.signOut()
    }
}
