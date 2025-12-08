package com.example.tpsqlite

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class ReportDetailsActivity : AppCompatActivity() {

    private lateinit var db: DataHelper
    private lateinit var report: Report

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_details)

        db = DataHelper(this)

        // Récupérer le report depuis l'intent
        val titre = intent.getStringExtra("titre") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val categorie = intent.getStringExtra("categorie") ?: ""
        val priorite = intent.getIntExtra("priorite", 3)

        report = Report(
            titre = titre,
            description = description,
            categorie = categorie,
            priorite = priorite
        )

        // Initialiser les vues
        val tvTitre = findViewById<TextView>(R.id.tvTitre)
        val tvDescription = findViewById<TextView>(R.id.tvDescription)
        val tvCategorie = findViewById<TextView>(R.id.tvCategorie)
        val tvPriorite = findViewById<TextView>(R.id.tvPriorite)
        val tvDate = findViewById<TextView>(R.id.tvDate)
        val lvComments = findViewById<ListView>(R.id.lvComments)
        val etNewComment = findViewById<EditText>(R.id.etNewComment)
        val btnAddComment = findViewById<Button>(R.id.btnAddComment)

        // Afficher les détails du report
        tvTitre.text = report.titre
        tvDescription.text = report.description
        tvCategorie.text = "Catégorie: ${report.categorie}"
        tvPriorite.text = "Priorité: ${report.priorite}/5"

        // Date de création (simulée pour l'exemple)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        tvDate.text = "Posté le: $currentDate"

        // Charger les commentaires
        loadComments(lvComments)

        // Ajouter un commentaire
        btnAddComment.setOnClickListener {
            val commentText = etNewComment.text.toString().trim()
            if (commentText.isNotEmpty()) {
                val currentUser = db.getCurrentUser()
                val userName = currentUser?.name ?: "Anonyme"

                if (db.addComment(report.titre, commentText, userName)) {
                    etNewComment.text.clear()
                    Toast.makeText(this, "Commentaire ajouté", Toast.LENGTH_SHORT).show()
                    loadComments(lvComments)
                } else {
                    Toast.makeText(this, "Erreur", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Veuillez saisir un commentaire", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadComments(listView: ListView) {
        val comments = db.getCommentsForReport(report.titre)

        if (comments.isEmpty()) {
            // Afficher un message si aucun commentaire
            val adapter = ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                listOf("Aucun commentaire. Soyez le premier à commenter!")
            )
            listView.adapter = adapter
        } else {
            // Afficher les commentaires avec un format personnalisé
            val commentTexts = comments.map { comment ->
                "👤 ${comment.user}\n📅 ${comment.date}\n${comment.text}"
            }

            val adapter = ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                commentTexts
            )
            listView.adapter = adapter
        }
    }
}