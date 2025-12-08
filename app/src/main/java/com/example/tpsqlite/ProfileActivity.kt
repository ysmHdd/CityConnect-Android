package com.example.tpsqlite

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnBack: Button
    private lateinit var db: DataHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvName = findViewById(R.id.tvProfileName)
        tvEmail = findViewById(R.id.tvProfileEmail)
        btnBack = findViewById(R.id.btnBackHome)
        db = DataHelper(this)

        val user = db.getCurrentUser()
        if (user != null) {
            tvName.text = "Nom : ${user.name}"
            tvEmail.text = "Email : ${user.email}"
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
