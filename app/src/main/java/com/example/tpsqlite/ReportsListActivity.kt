package com.example.tpsqlite

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView

class ReportsListActivity : AppCompatActivity() {

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

        // Gérer le clic sur un report pour voir les détails
        lvReports.setOnItemClickListener { parent, view, position, id ->
            val report = db.getAllReports()[position]
            val intent = Intent(this, ReportDetailsActivity::class.java).apply {
                putExtra("titre", report.titre)
                putExtra("description", report.description)
                putExtra("categorie", report.categorie)
                putExtra("priorite", report.priorite)
                putExtra("photoUri", report.photoUri) // Ajouter le chemin de l'image
            }
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