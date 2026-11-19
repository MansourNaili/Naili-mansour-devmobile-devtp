package com.example.contactsapp.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ContactDbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "contacts.db"
        const val DATABASE_VERSION = 1

        @Volatile
        private var instance: ContactDbHelper? = null


        fun getInstance(context: Context): ContactDbHelper {
            return instance ?: synchronized(this) {
                instance ?: ContactDbHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE ${ContactContract.TABLE_NAME} (" +
                    "${ContactContract.COL_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "${ContactContract.COL_NOM} TEXT NOT NULL, " +
                    "${ContactContract.COL_PRENOM} TEXT NOT NULL, " +
                    "${ContactContract.COL_TELEPHONE} TEXT NOT NULL, " +
                    "${ContactContract.COL_EMAIL} TEXT, " +
                    "${ContactContract.COL_PHOTO_URI} TEXT, " +
                    "${ContactContract.COL_LATITUDE} REAL, " +
                    "${ContactContract.COL_LONGITUDE} REAL" +
                    ")"
        )
        insererContactsDemo(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${ContactContract.TABLE_NAME}")
        onCreate(db)
    }

    private fun insererContactsDemo(db: SQLiteDatabase) {
        val contacts = listOf(
            Contact(
                nom = "Dupont", prenom = "Claire", telephone = "06 12 34 56 78",
                email = "claire.dupont@example.com", latitude = 33.8076, longitude = 10.8451
            ),
            Contact(
                nom = "Martin", prenom = "Thomas", telephone = "07 23 45 67 89",
                email = "thomas.martin@example.com", latitude = 36.8065, longitude = 10.1815
            ),
            Contact(
                nom = "Bernard", prenom = "Sophie", telephone = "06 98 76 54 32",
                email = "sophie.bernard@example.com", latitude = 34.7406, longitude = 10.7603
            ),
            Contact(
                nom = "Petit", prenom = "Lucas", telephone = "07 11 22 33 44",
                email = "lucas.petit@example.com", latitude = 35.8256, longitude = 10.6084
            ),
            Contact(
                nom = "Moreau", prenom = "Julien", telephone = "06 55 44 33 22",
                email = "julien.moreau@example.com"
            ),
            Contact(
                nom = "Ben Ali", prenom = "Sami", telephone = "98 123 456",
                email = "sami.benali@example.com"
            )
        )

        db.beginTransaction()
        try {
            for (contact in contacts) {
                db.insert(ContactContract.TABLE_NAME, null, contact.toContentValues())
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}