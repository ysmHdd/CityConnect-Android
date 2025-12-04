package com.example.tpsqlite

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AddReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_report)

        val etTitre = findViewById<EditText>(R.id.etTitre)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val spinnerCategorie = findViewById<Spinner>(R.id.spinnerCategorie)
        val etPriorite = findViewById<EditText>(R.id.etPriorite)
        val btnSave = findViewById<Button>(R.id.btnSaveReport)

        val db = DataHelper(this)

        // Spinner categories
        val categories = arrayOf("infrastructure", "environnement", "securite", "proprete", "urbanisme", "eau", "transport", "autre")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategorie.adapter = adapter

        btnSave.setOnClickListener {
            val titre = etTitre.text.toString()
            val description = etDescription.text.toString()
            val categorie = spinnerCategorie.selectedItem.toString()
            val priorite = etPriorite.text.toString().toIntOrNull() ?: 3

            if (titre.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val report = Report(
                titre = titre,
                description = description,
                categorie = categorie,
                priorite = priorite
            )

            db.addReport(report)
            finish()
        }
    }
}
