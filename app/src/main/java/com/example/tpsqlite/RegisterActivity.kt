package com.example.tpsqlite

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var db: DataHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        db = DataHelper(this)

        btnRegister.setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Remplis tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (db.addUser(name, email, password)) {

                val user = User(name, email)
                db.setUserLoggedIn(user)
                Toast.makeText(this, "Compte créé avec succès !", Toast.LENGTH_SHORT).show()
                finish()  // Retour à LoginActivity (qui détectera auto-login)
            } else {
                Toast.makeText(this, "Email déjà utilisé", Toast.LENGTH_SHORT).show()
            }
        }
    }
}