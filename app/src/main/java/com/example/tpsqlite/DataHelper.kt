package com.example.tpsqlite

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

class DataHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "mabase_reports.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_REPORT = "REPORT"
        const val KEY_TITRE = "TITRE"
        const val KEY_DESCRIPTION = "DESCRIPTION"
        const val KEY_CATEGORIE = "CATEGORIE"
        const val KEY_SOUSCATEGORIE = "SOUSCATEGORIE"
        const val KEY_STATUT = "STATUT"
        const val KEY_PRIORITE = "PRIORITE"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_TABLE = ("CREATE TABLE $TABLE_REPORT (" +
                "$KEY_TITRE TEXT PRIMARY KEY, " +
                "$KEY_DESCRIPTION TEXT, " +
                "$KEY_CATEGORIE TEXT, " +
                "$KEY_SOUSCATEGORIE TEXT, " +
                "$KEY_STATUT TEXT, " +
                "$KEY_PRIORITE INTEGER)")
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_REPORT")
        onCreate(db)
    }

    fun addReport(report: Report): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_TITRE, report.titre)
            put(KEY_DESCRIPTION, report.description)
            put(KEY_CATEGORIE, report.categorie)
            put(KEY_SOUSCATEGORIE, report.sousCategorie)
            put(KEY_STATUT, report.statut)
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
            put(KEY_SOUSCATEGORIE, report.sousCategorie)
            put(KEY_STATUT, report.statut)
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
                        titre = cursor.getString(0),
                        description = cursor.getString(1),
                        categorie = cursor.getString(2),
                        sousCategorie = cursor.getString(3),
                        statut = cursor.getString(4),
                        priorite = cursor.getInt(5)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }
}
