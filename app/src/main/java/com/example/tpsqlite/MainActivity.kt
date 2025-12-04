package com.example.tpsqlite

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView

class MainActivity : AppCompatActivity() {

    lateinit var lvReports: ListView
    lateinit var btnAdd: Button
    lateinit var db: DataHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lvReports = findViewById(R.id.lvReports)
        btnAdd = findViewById(R.id.btnAddReport)
        db = DataHelper(this)

        loadReports()

        btnAdd.setOnClickListener {
            val intent = Intent(this, AddReportActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadReports()
    }

    private fun loadReports() {
        val list = db.getAllReports()
        val adapter = ReportAdapter(this, list)
        lvReports.adapter = adapter
    }
}
