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
        private const val DATABASE_VERSION = 5

        // Table REPORT
        const val TABLE_REPORT = "REPORT"
        const val KEY_ID = "ID"
        const val KEY_TITRE = "TITRE"
        const val KEY_DESCRIPTION = "DESCRIPTION"
        const val KEY_CATEGORIE = "CATEGORIE"
        const val KEY_PRIORITE = "PRIORITE"

        // Table USER
        const val TABLE_USER = "USER"
        const val KEY_USER_ID = "ID"
        const val KEY_NAME = "NAME"
        const val KEY_EMAIL = "EMAIL"
        const val KEY_PASSWORD = "PASSWORD"
        const val KEY_PROFILE_PHOTO = "PROFILE_PHOTO"

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
        val CREATE_TABLE_REPORT = """
            CREATE TABLE $TABLE_REPORT (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_TITRE TEXT UNIQUE,
                $KEY_DESCRIPTION TEXT,
                $KEY_CATEGORIE TEXT,
                $KEY_PRIORITE INTEGER
            )
        """.trimIndent()
        db.execSQL(CREATE_TABLE_REPORT)

        // Table des utilisateurs
        val CREATE_TABLE_USER = """
            CREATE TABLE $TABLE_USER (
                $KEY_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_NAME TEXT,
                $KEY_EMAIL TEXT UNIQUE,
                $KEY_PASSWORD TEXT,
                $KEY_PROFILE_PHOTO TEXT
            )
        """.trimIndent()
        db.execSQL(CREATE_TABLE_USER)

        // Table de session
        val CREATE_TABLE_SESSION = """
            CREATE TABLE $TABLE_SESSION (
                $KEY_SESSION_ID INTEGER PRIMARY KEY,
                $KEY_LOGGED_IN INTEGER,
                $KEY_USER_ID_FK INTEGER
            )
        """.trimIndent()
        db.execSQL(CREATE_TABLE_SESSION)

        // Table des commentaires
        val CREATE_TABLE_COMMENTS = """
            CREATE TABLE $TABLE_COMMENTS (
                $KEY_COMMENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_REPORT_TITRE TEXT,
                $KEY_COMMENT_TEXT TEXT,
                $KEY_COMMENT_USER TEXT,
                $KEY_COMMENT_DATE TEXT,
                FOREIGN KEY($KEY_REPORT_TITRE) REFERENCES $TABLE_REPORT($KEY_TITRE) ON DELETE CASCADE
            )
        """.trimIndent()
        db.execSQL(CREATE_TABLE_COMMENTS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Gestion des upgrades par version
        if (oldVersion < 2) {
            // Mises à jour pour la version 2
        }
        if (oldVersion < 3) {
            // Mises à jour pour la version 3
        }
        if (oldVersion < 4) {
            // Ajout de la colonne PROFILE_PHOTO pour la version 4
            db.execSQL("ALTER TABLE $TABLE_USER ADD COLUMN $KEY_PROFILE_PHOTO TEXT")
        }
        if (oldVersion < 5) {
            // Mises à jour pour la version 5
            // Pas de changements structurels nécessaires
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        db.execSQL("DROP TABLE IF EXISTS $TABLE_REPORT")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SESSION")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COMMENTS")
        onCreate(db)
    }


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

        db.delete(TABLE_COMMENTS, "$KEY_REPORT_TITRE = ?", arrayOf(report.titre))

        db.close()

        Toast.makeText(context, "Report supprimé: ${report.titre}", Toast.LENGTH_SHORT).show()
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



    fun addUser(name: String, email: String, password: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_NAME, name)
            put(KEY_EMAIL, email)
            put(KEY_PASSWORD, password)

        }

        return try {
            val result = db.insert(TABLE_USER, null, values)
            result != -1L
        } catch (e: SQLiteConstraintException) {
            false
        } finally {
            db.close()
        }
    }

    fun checkUser(email: String, password: String): Boolean {
        val db = this.readableDatabase
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
            val profilePhoto = if (!cursor.isNull(cursor.getColumnIndexOrThrow(KEY_PROFILE_PHOTO))) {
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_PHOTO))
            } else {
                ""
            }
            User(name, userEmail, profilePhoto)
        } else {
            null
        }.also {
            cursor.close()
            db.close()
        }
    }


    fun updateUserProfilePhoto(email: String, photoUri: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_PROFILE_PHOTO, photoUri)
        }

        val success = db.update(TABLE_USER, values, "$KEY_EMAIL = ?", arrayOf(email))
        db.close()

        return if (success > 0) {
            Toast.makeText(context, "Photo de profil mise à jour", Toast.LENGTH_SHORT).show()
            true
        } else {
            Toast.makeText(context, "Erreur de mise à jour", Toast.LENGTH_SHORT).show()
            false
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
                SELECT u.$KEY_NAME, u.$KEY_EMAIL, u.$KEY_PROFILE_PHOTO 
                FROM $TABLE_SESSION s 
                JOIN $TABLE_USER u ON s.$KEY_USER_ID_FK = u.$KEY_USER_ID 
                WHERE s.$KEY_LOGGED_IN = 1
            """.trimIndent()

            val cursor = db.rawQuery(query, null)

            if (cursor.moveToFirst()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME))
                val email = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL))
                val profilePhoto = if (!cursor.isNull(cursor.getColumnIndexOrThrow(KEY_PROFILE_PHOTO))) {
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_PROFILE_PHOTO))
                } else {
                    ""
                }
                User(name, email, profilePhoto)
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
            db.close()
        }
    }

    fun logout() {
        val db = this.writableDatabase
        db.delete(TABLE_SESSION, null, null)
        db.close()
    }

}

// ==================== MODÈLES DE DONNÉES ====================
data class User(
    val name: String,
    val email: String,
    val profilePhotoUri: String = ""
)

data class Comment(
    val id: Int = 0,
    val reportTitre: String = "",
    val text: String = "",
    val user: String = "",
    val date: String = ""
)

