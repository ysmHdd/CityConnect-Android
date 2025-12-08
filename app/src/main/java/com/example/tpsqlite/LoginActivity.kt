package com.example.tpsqlite

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var db: DataHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        db = DataHelper(this)

        // Auto-login si déjà connecté
        if (db.isUserLoggedIn()) {
            goToHome()
            return
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Remplis tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (db.checkUser(email, password)) {

                val user = db.getUserByEmail(email)
                if (user != null) {

                    db.setUserLoggedIn(user)
                    Toast.makeText(this, "Connexion réussie !", Toast.LENGTH_SHORT).show()
                    goToHome()
                } else {
                    Toast.makeText(this, "Erreur de récupération du profil", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun goToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}