package com.example.tpsqlite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

class DataHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "mabase_reports.db"
        private const val DATABASE_VERSION = 5 // Augmenter à 5 pour forcer la recréation

        // Table REPORT
        const val TABLE_REPORT = "REPORT"
        const val KEY_TITRE = "TITRE"
        const val KEY_DESCRIPTION = "DESCRIPTION"
        const val KEY_CATEGORIE = "CATEGORIE"
        const val KEY_PRIORITE = "PRIORITE"

        // Table USER - REMETTRE LES ANCIENS NOMS DE COLONNES
        const val TABLE_USER = "USER"
        const val KEY_USER_ID = "ID"
        const val KEY_NAME = "NAME"        // Ancien nom
        const val KEY_EMAIL = "EMAIL"      // Ancien nom
        const val KEY_PASSWORD = "PASSWORD" // Ancien nom

        // Table SESSION
        const val TABLE_SESSION = "SESSION"
        const val KEY_SESSION_ID = "ID"
        const val KEY_LOGGED_IN = "LOGGED_IN"
        const val KEY_USER_ID_FK = "USER_ID"

        // Table COMMENTAIRES
        const val TABLE_COMMENTS = "COMMENTS"
        const val KEY_COMMENT_ID = "COMMENT_ID"
        const val KEY_REPORT_TITRE = "REPORT_TITRE"
        const val KEY_COMMENT_TEXT = "COMMENT_TEXT"
        const val KEY_COMMENT_USER = "COMMENT_USER"
        const val KEY_COMMENT_DATE = "COMMENT_DATE"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Table des rapports
        val CREATE_TABLE_REPORT = ("CREATE TABLE $TABLE_REPORT (" +
                "$KEY_TITRE TEXT PRIMARY KEY, " +
                "$KEY_DESCRIPTION TEXT, " +
                "$KEY_CATEGORIE TEXT, " +
                "$KEY_PRIORITE INTEGER)")
        db.execSQL(CREATE_TABLE_REPORT)

        // Table des utilisateurs - AVEC LES ANCIENS NOMS DE COLONNES
        val CREATE_TABLE_USER = ("CREATE TABLE $TABLE_USER (" +
                "$KEY_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$KEY_NAME TEXT, " +
                "$KEY_EMAIL TEXT UNIQUE, " +
                "$KEY_PASSWORD TEXT)")
        db.execSQL(CREATE_TABLE_USER)

        // Table de session
        val CREATE_TABLE_SESSION = ("CREATE TABLE $TABLE_SESSION (" +
                "$KEY_SESSION_ID INTEGER PRIMARY KEY, " +
                "$KEY_LOGGED_IN INTEGER, " +
                "$KEY_USER_ID_FK INTEGER)")
        db.execSQL(CREATE_TABLE_SESSION)

        // Table des commentaires
        val CREATE_TABLE_COMMENTS = ("CREATE TABLE $TABLE_COMMENTS (" +
                "$KEY_COMMENT_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$KEY_REPORT_TITRE TEXT, " +
                "$KEY_COMMENT_TEXT TEXT, " +
                "$KEY_COMMENT_USER TEXT, " +
                "$KEY_COMMENT_DATE TEXT, " +
                "FOREIGN KEY($KEY_REPORT_TITRE) REFERENCES $TABLE_REPORT($KEY_TITRE) ON DELETE CASCADE)")
        db.execSQL(CREATE_TABLE_COMMENTS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // SIMPLIFIÉ : Supprimer et recréer toutes les tables
        db.execSQL("DROP TABLE IF EXISTS $TABLE_REPORT")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SESSION")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COMMENTS")
        onCreate(db)
    }

    // ================ MÉTHODES POUR LES COMMENTAIRES ================
    fun addComment(reportTitre: String, commentText: String, userName: String): Boolean {
        val db = this.writableDatabase
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val values = ContentValues().apply {
            put(KEY_REPORT_TITRE, reportTitre)
            put(KEY_COMMENT_TEXT, commentText)
            put(KEY_COMMENT_USER, userName)
            put(KEY_COMMENT_DATE, currentDate)
        }

        val success = db.insert(TABLE_COMMENTS, null, values)
        db.close()
        return success != -1L
    }

    fun getCommentsForReport(reportTitre: String): ArrayList<Comment> {
        val db = this.readableDatabase
        val list = ArrayList<Comment>()
        val query = "SELECT * FROM $TABLE_COMMENTS WHERE $KEY_REPORT_TITRE = ? ORDER BY $KEY_COMMENT_DATE DESC"
        val cursor = db.rawQuery(query, arrayOf(reportTitre))

        if (cursor.moveToFirst()) {
            do {
                list.add(
                    Comment(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_COMMENT_ID)),
                        reportTitre = cursor.getString(cursor.getColumnIndexOrThrow(KEY_REPORT_TITRE)),
                        text = cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMMENT_TEXT)),
                        user = cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMMENT_USER)),
                        date = cursor.getString(cursor.getColumnIndexOrThrow(KEY_COMMENT_DATE))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    fun getCommentCountForReport(reportTitre: String): Int {
        val db = this.readableDatabase
        val query = "SELECT COUNT(*) FROM $TABLE_COMMENTS WHERE $KEY_REPORT_TITRE = ?"
        val cursor = db.rawQuery(query, arrayOf(reportTitre))
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        db.close()
        return count
    }

    fun deleteComment(commentId: Int): Boolean {
        val db = this.writableDatabase
        val success = db.delete(TABLE_COMMENTS, "$KEY_COMMENT_ID = ?", arrayOf(commentId.toString()))
        db.close()
        return success > 0
    }

    // ================ MÉTHODES POUR LES REPORTS ================
    fun addReport(report: Report): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_TITRE, report.titre)
            put(KEY_DESCRIPTION, report.description)
            put(KEY_CATEGORIE, report.categorie)
            put(KEY_PRIORITE, report.priorite)
        }
        val success = db.insert(TABLE_REPORT, null, values)
        db.close()
        return if (success == -1L) {
            Toast.makeText(context, "Échec d'insertion du report", Toast.LENGTH_SHORT).show()
            false
        } else {
            Toast.makeText(context, "Report ajouté avec succès", Toast.LENGTH_SHORT).show()
            true
        }
    }

    fun updateReport(report: Report): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_DESCRIPTION, report.description)
            put(KEY_CATEGORIE, report.categorie)
            put(KEY_PRIORITE, report.priorite)
        }
        val success = db.update(TABLE_REPORT, values, "$KEY_TITRE = ?", arrayOf(report.titre))
        db.close()
        return success > 0
    }

    fun deleteReport(report: Report) {
        val db = this.writableDatabase
        db.delete(TABLE_REPORT, "$KEY_TITRE = ?", arrayOf(report.titre))
        db.close()
    }

    fun getAllReports(): ArrayList<Report> {
        val db = this.readableDatabase
        val list = ArrayList<Report>()
        val cursor = db.rawQuery("SELECT * FROM $TABLE_REPORT", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    Report(
                        titre = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITRE)),
                        description = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)),
                        categorie = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORIE)),
                        priorite = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PRIORITE))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    // ================ MÉTHODES POUR LES UTILISATEURS ================

    fun addUser(name: String, email: String, password: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_NAME, name)        // KEY_NAME = "NAME"
            put(KEY_EMAIL, email)      // KEY_EMAIL = "EMAIL"
            put(KEY_PASSWORD, password) // KEY_PASSWORD = "PASSWORD"
        }

        return try {
            val result = db.insert(TABLE_USER, null, values)  // TABLE_USER = "USER"
            result != -1L
        } catch (e: SQLiteConstraintException) {
            // Email déjà utilisé
            false
        } finally {
            db.close()
        }
    }

    fun checkUser(email: String, password: String): Boolean {
        val db = this.readableDatabase
        // CORRECT : TABLE_USER = "USER", KEY_EMAIL = "EMAIL", KEY_PASSWORD = "PASSWORD"
        val query = "SELECT * FROM $TABLE_USER WHERE $KEY_EMAIL = ? AND $KEY_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(email, password))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun getUserByEmail(email: String): User? {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USER WHERE $KEY_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(email))

        return if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME))
            val userEmail = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL))
            User(name, userEmail)
        } else {
            null
        }.also {
            cursor.close()
            db.close()
        }
    }

    fun setUserLoggedIn(user: User?) {
        val db = this.writableDatabase
        db.delete(TABLE_SESSION, null, null)

        if (user != null) {
            val userId = getUserIdByEmail(user.email)
            if (userId != -1) {
                val values = ContentValues().apply {
                    put(KEY_LOGGED_IN, 1)
                    put(KEY_USER_ID_FK, userId)
                }
                db.insert(TABLE_SESSION, null, values)
            }
        }
        db.close()
    }

    fun isUserLoggedIn(): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_SESSION WHERE $KEY_LOGGED_IN = 1"
        val cursor = db.rawQuery(query, null)
        val loggedIn = cursor.count > 0
        cursor.close()
        db.close()
        return loggedIn
    }

    fun getCurrentUser(): User? {
        return try {
            val db = this.readableDatabase
            val query = """
                SELECT u.$KEY_NAME, u.$KEY_EMAIL 
                FROM $TABLE_SESSION s 
                JOIN $TABLE_USER u ON s.$KEY_USER_ID_FK = u.$KEY_USER_ID 
                WHERE s.$KEY_LOGGED_IN = 1
            """.trimIndent()

            val cursor = db.rawQuery(query, null)

            if (cursor.moveToFirst()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME))
                val email = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL))
                User(name, email)
            } else {
                null
            }.also {
                cursor.close()
                db.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getUserIdByEmail(email: String): Int {
        val db = this.readableDatabase
        val query = "SELECT $KEY_USER_ID FROM $TABLE_USER WHERE $KEY_EMAIL = ?"
        val cursor = db.rawQuery(query, arrayOf(email))

        return if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndexOrThrow(KEY_USER_ID))
        } else {
            -1
        }.also {
            cursor.close()
        }
    }

    fun logout() {
        val db = this.writableDatabase
        db.delete(TABLE_SESSION, null, null)
        db.close()
    }
}

// ==================== MODÈLES DE DONNÉES ====================

data class User(val name: String, val email: String)
data class Comment(
    val id: Int = 0,
    val reportTitre: String = "",
    val text: String = "",
    val user: String = "",
    val date: String = ""
)