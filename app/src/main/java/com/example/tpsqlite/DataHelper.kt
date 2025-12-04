package com.example.tpsqlite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

class DataHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "mabase_reports.db"
        private const val DATABASE_VERSION = 2

        // Table REPORT (SIMPLIFIÉE - 4 champs)
        const val TABLE_REPORT = "REPORT"
        const val KEY_TITRE = "TITRE"
        const val KEY_DESCRIPTION = "DESCRIPTION"
        const val KEY_CATEGORIE = "CATEGORIE"
        const val KEY_PRIORITE = "PRIORITE"

        // Table USERS
        const val TABLE_USERS = "USERS"
        const val KEY_USER_ID = "ID"
        const val KEY_NAME = "NAME"
        const val KEY_EMAIL = "EMAIL"
        const val KEY_PASSWORD = "PASSWORD"

        // Table SESSION
        const val TABLE_SESSION = "SESSION"
        const val KEY_SESSION_ID = "ID"
        const val KEY_LOGGED_IN = "LOGGED_IN"
        const val KEY_USER_ID_FK = "USER_ID"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Créer table REPORTS (SIMPLIFIÉE)
        val CREATE_TABLE_REPORT = ("CREATE TABLE $TABLE_REPORT (" +
                "$KEY_TITRE TEXT PRIMARY KEY, " +
                "$KEY_DESCRIPTION TEXT, " +
                "$KEY_CATEGORIE TEXT, " +
                "$KEY_PRIORITE INTEGER)")
        db.execSQL(CREATE_TABLE_REPORT)

        // Créer table USERS
        val CREATE_TABLE_USERS = ("CREATE TABLE $TABLE_USERS (" +
                "$KEY_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$KEY_NAME TEXT, " +
                "$KEY_EMAIL TEXT UNIQUE, " +
                "$KEY_PASSWORD TEXT)")
        db.execSQL(CREATE_TABLE_USERS)

        // Créer table SESSION
        val CREATE_TABLE_SESSION = ("CREATE TABLE $TABLE_SESSION (" +
                "$KEY_SESSION_ID INTEGER PRIMARY KEY, " +
                "$KEY_LOGGED_IN INTEGER, " +
                "$KEY_USER_ID_FK INTEGER)")
        db.execSQL(CREATE_TABLE_SESSION)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Ajouter les nouvelles tables sans supprimer l'ancienne
            val CREATE_TABLE_USERS = ("CREATE TABLE IF NOT EXISTS $TABLE_USERS (" +
                    "$KEY_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$KEY_NAME TEXT, " +
                    "$KEY_EMAIL TEXT UNIQUE, " +
                    "$KEY_PASSWORD TEXT)")
            db.execSQL(CREATE_TABLE_USERS)

            val CREATE_TABLE_SESSION = ("CREATE TABLE IF NOT EXISTS $TABLE_SESSION (" +
                    "$KEY_SESSION_ID INTEGER PRIMARY KEY, " +
                    "$KEY_LOGGED_IN INTEGER, " +
                    "$KEY_USER_ID_FK INTEGER)")
            db.execSQL(CREATE_TABLE_SESSION)
        }
    }

    // ==================== METHODES POUR REPORTS ====================

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

    // ==================== METHODES POUR USERS ====================

    fun addUser(name: String, email: String, password: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_NAME, name)
            put(KEY_EMAIL, email)
            put(KEY_PASSWORD, password)
        }

        return try {
            val result = db.insert(TABLE_USERS, null, values)
            result != -1L
        } catch (e: SQLiteConstraintException) {
            false
        } finally {
            db.close()
        }
    }

    fun checkUser(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $KEY_EMAIL = ? AND $KEY_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(email, password))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun getUserByEmail(email: String): User? {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $KEY_EMAIL = ?"
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

    // ==================== METHODES POUR SESSION ====================

    fun setUserLoggedIn(user: User?) {
        val db = this.writableDatabase
        db.delete(TABLE_SESSION, null, null)

        if (user != null) {
            val values = ContentValues().apply {
                put(KEY_LOGGED_IN, 1)
                val userId = getUserIdByEmail(user.email)
                if (userId != -1) {
                    put(KEY_USER_ID_FK, userId)
                }
            }
            db.insert(TABLE_SESSION, null, values)
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
        val db = this.readableDatabase
        val query = """
            SELECT u.$KEY_NAME, u.$KEY_EMAIL 
            FROM $TABLE_SESSION s 
            JOIN $TABLE_USERS u ON s.$KEY_USER_ID_FK = u.$KEY_USER_ID 
            WHERE s.$KEY_LOGGED_IN = 1
        """.trimIndent()

        val cursor = db.rawQuery(query, null)

        return if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME))
            val email = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL))
            User(name, email)
        } else {
            null
        }.also {
            cursor.close()
            db.close()
        }
    }

    private fun getUserIdByEmail(email: String): Int {
        val db = this.readableDatabase
        val query = "SELECT $KEY_USER_ID FROM $TABLE_USERS WHERE $KEY_EMAIL = ?"
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

// ==================== MODELES DE DONNEES ====================
// UNE SEULE CLASSE REPORT - VERSION SIMPLE

/*data class Report(
    val titre: String,
    val description: String,
    val categorie: String,
    val priorite: Int
)*/

data class User(val name: String, val email: String)