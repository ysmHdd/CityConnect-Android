package com.example.tpsqlite

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvUserInfo: TextView
    private lateinit var btnViewReports: Button
    private lateinit var btnLogout: Button
    private lateinit var db: DataHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        tvWelcome = findViewById(R.id.tvWelcome)
        tvUserInfo = findViewById(R.id.tvUserInfo)
        btnViewReports = findViewById(R.id.btnViewReports)
        btnLogout = findViewById(R.id.btnLogout)
        db = DataHelper(this)

        // Afficher infos utilisateur
        val currentUser = db.getCurrentUser()
        if (currentUser != null) {
            tvUserInfo.text = "Connecté en tant que: ${currentUser.name}"
        }

        btnViewReports.setOnClickListener {
            val intent = Intent(this, ReportsListActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            db.logout()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}