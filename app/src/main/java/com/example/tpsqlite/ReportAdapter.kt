package com.example.tpsqlite

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

class ReportAdapter(
    private val context: Context,
    private val reports: ArrayList<Report>
) : BaseAdapter() {

    override fun getCount(): Int = reports.size
    override fun getItem(position: Int): Any = reports[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.report_item, parent, false)

        // Initialiser toutes les vues
        val tvInfo = view.findViewById<TextView>(R.id.tvReportInfo)
        val btnUpdate = view.findViewById<Button>(R.id.btnUpdateReport)
        val btnViewDetails = view.findViewById<Button>(R.id.btnViewDetails)
        val btnToggleComments = view.findViewById<Button>(R.id.btnToggleComments)
        val layoutCommentsSection = view.findViewById<LinearLayout>(R.id.layoutCommentsSection)
        val lvComments = view.findViewById<ListView>(R.id.lvComments)
        val etNewComment = view.findViewById<EditText>(R.id.etNewComment)
        val btnAddComment = view.findViewById<Button>(R.id.btnAddComment)

        val report = reports[position]
        val db = DataHelper(context)

        // Afficher les infos du rapport
        tvInfo.text = "${report.titre} - ${report.categorie} - Priorité: ${report.priorite}/5"

        // Mettre à jour le bouton avec le nombre de commentaires
        val commentCount = try {
            db.getCommentCountForReport(report.titre)
        } catch (e: Exception) {
            0
        }
        btnToggleComments.text = "💬 Commentaires ($commentCount)"

        // ===== BOUTON "VOIR DÉTAILS" =====
        btnViewDetails.setOnClickListener {
            val intent = Intent(context, ReportDetailsActivity::class.java).apply {
                putExtra("titre", report.titre)
                putExtra("description", report.description)
                putExtra("categorie", report.categorie)
                putExtra("priorite", report.priorite)
            }
            context.startActivity(intent)
        }

        // ===== BOUTON "MODIFIER" =====
        btnUpdate.setOnClickListener {
            val intent = Intent(context, AddReportActivity::class.java).apply {
                putExtra("titre", report.titre)
                putExtra("description", report.description)
                putExtra("categorie", report.categorie)
                putExtra("priorite", report.priorite)
            }
            context.startActivity(intent)
        }

        // ===== GESTION DES COMMENTAIRES INLINE =====
        // Initialiser l'adapter des commentaires
        val commentAdapter = CommentAdapter(context, ArrayList())
        lvComments.adapter = commentAdapter

        // Charger les commentaires au démarrage (optionnel)
        if (commentCount > 0) {
            loadComments(report.titre, commentAdapter, db)
        }

        // Toggle pour afficher/masquer les commentaires
        btnToggleComments.setOnClickListener {
            val isVisible = layoutCommentsSection.visibility == View.VISIBLE
            if (isVisible) {
                layoutCommentsSection.visibility = View.GONE
                btnToggleComments.text = "💬 Commentaires ($commentCount)"
            } else {
                layoutCommentsSection.visibility = View.VISIBLE
                btnToggleComments.text = "▼ Masquer"
                loadComments(report.titre, commentAdapter, db)
            }
        }

        // Ajouter un nouveau commentaire
        btnAddComment.setOnClickListener {
            val commentText = etNewComment.text.toString().trim()
            if (commentText.isNotEmpty()) {
                val currentUser = db.getCurrentUser()
                val userName = currentUser?.name ?: "Anonyme"

                if (db.addComment(report.titre, commentText, userName)) {
                    etNewComment.text.clear()
                    Toast.makeText(context, "Commentaire ajouté", Toast.LENGTH_SHORT).show()

                    // Recharger les commentaires et mettre à jour le compteur
                    loadComments(report.titre, commentAdapter, db)
                    val newCount = db.getCommentCountForReport(report.titre)
                    btnToggleComments.text = "▼ Masquer ($newCount)"
                } else {
                    Toast.makeText(context, "Erreur", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Saisissez un commentaire", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun loadComments(reportTitre: String, adapter: CommentAdapter, db: DataHelper) {
        val comments = try {
            db.getCommentsForReport(reportTitre)
        } catch (e: Exception) {
            ArrayList()
        }
        adapter.updateData(comments)

        // Ajuster la hauteur de la ListView
        val totalHeight = minOf(comments.size * 120, 300) // Max 300dp
        adapter.notifyDataSetChanged()
    }
}

// ===== ADAPTER POUR LES COMMENTAIRES =====
class CommentAdapter(
    private val context: Context,
    private var comments: ArrayList<Comment>
) : BaseAdapter() {

    fun updateData(newComments: ArrayList<Comment>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }

    override fun getCount(): Int = comments.size
    override fun getItem(position: Int): Any = comments[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.comment_item, parent, false)

        val comment = comments[position]

        val tvUser = view.findViewById<TextView>(R.id.tvCommentUser)
        val tvDate = view.findViewById<TextView>(R.id.tvCommentDate)
        val tvText = view.findViewById<TextView>(R.id.tvCommentText)

        tvUser.text = comment.user
        tvDate.text = comment.date
        tvText.text = comment.text

        return view
    }
}