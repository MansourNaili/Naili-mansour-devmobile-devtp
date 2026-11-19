package com.example.contactsapp.data

import android.content.ContentValues
import android.database.Cursor

internal fun Contact.toContentValues(): ContentValues {
    val valeurs = ContentValues()
    valeurs.put(ContactContract.COL_NOM, nom)
    valeurs.put(ContactContract.COL_PRENOM, prenom)
    valeurs.put(ContactContract.COL_TELEPHONE, telephone)
    valeurs.put(ContactContract.COL_EMAIL, email)
    valeurs.put(ContactContract.COL_PHOTO_URI, photoUri)
    valeurs.put(ContactContract.COL_LATITUDE, latitude)
    valeurs.put(ContactContract.COL_LONGITUDE, longitude)
    return valeurs
}

class ContactDao(private val dbHelper: ContactDbHelper) {

    private val ordre = "${ContactContract.COL_NOM} COLLATE NOCASE ASC, " +
            "${ContactContract.COL_PRENOM} COLLATE NOCASE ASC"

    fun insert(contact: Contact): Long {
        val db = dbHelper.writableDatabase
        return db.insert(ContactContract.TABLE_NAME, null, contact.toContentValues())
    }

    fun update(contact: Contact): Int {
        val db = dbHelper.writableDatabase
        return db.update(
            ContactContract.TABLE_NAME,
            contact.toContentValues(),
            "${ContactContract.COL_ID} = ?",
            arrayOf(contact.id.toString())
        )
    }

    fun delete(id: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            ContactContract.TABLE_NAME,
            "${ContactContract.COL_ID} = ?",
            arrayOf(id.toString())
        )
    }

    fun getAll(): List<Contact> {
        val db = dbHelper.readableDatabase
        db.query(ContactContract.TABLE_NAME, null, null, null, null, null, ordre).use { cursor ->
            return lireListe(cursor)
        }
    }

    fun getById(id: Long): Contact? {
        val db = dbHelper.readableDatabase
        db.query(
            ContactContract.TABLE_NAME,
            null,
            "${ContactContract.COL_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use { cursor ->
            return if (cursor.moveToFirst()) versContact(cursor) else null
        }
    }

    fun search(texte: String): List<Contact> {
        if (texte.isBlank()) return getAll()

        val motif = "%" + echapper(texte.trim()) + "%"
        val motifTelephone = "%" + echapper(texte.replace(" ", "")) + "%"

        val selection =
            "${ContactContract.COL_NOM} LIKE ? ESCAPE '\\' OR " +
                    "${ContactContract.COL_PRENOM} LIKE ? ESCAPE '\\' OR " +
                    "(${ContactContract.COL_NOM} || ' ' || ${ContactContract.COL_PRENOM}) LIKE ? ESCAPE '\\' OR " +
                    "(${ContactContract.COL_PRENOM} || ' ' || ${ContactContract.COL_NOM}) LIKE ? ESCAPE '\\' OR " +
                    "REPLACE(${ContactContract.COL_TELEPHONE}, ' ', '') LIKE ? ESCAPE '\\'"

        val db = dbHelper.readableDatabase
        db.query(
            ContactContract.TABLE_NAME,
            null,
            selection,
            arrayOf(motif, motif, motif, motif, motifTelephone),
            null,
            null,
            ordre
        ).use { cursor ->
            return lireListe(cursor)
        }
    }

    private fun echapper(texte: String): String {
        return texte.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
    }

    private fun lireListe(cursor: Cursor): List<Contact> {
        val liste = mutableListOf<Contact>()
        while (cursor.moveToNext()) {
            liste.add(versContact(cursor))
        }
        return liste
    }

    private fun versContact(cursor: Cursor): Contact {
        val indexEmail = cursor.getColumnIndexOrThrow(ContactContract.COL_EMAIL)
        val indexPhoto = cursor.getColumnIndexOrThrow(ContactContract.COL_PHOTO_URI)
        val indexLatitude = cursor.getColumnIndexOrThrow(ContactContract.COL_LATITUDE)
        val indexLongitude = cursor.getColumnIndexOrThrow(ContactContract.COL_LONGITUDE)

        return Contact(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactContract.COL_ID)),
            nom = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.COL_NOM)),
            prenom = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.COL_PRENOM)),
            telephone = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.COL_TELEPHONE)),
            email = if (cursor.isNull(indexEmail)) null else cursor.getString(indexEmail),
            photoUri = if (cursor.isNull(indexPhoto)) null else cursor.getString(indexPhoto),
            latitude = if (cursor.isNull(indexLatitude)) null else cursor.getDouble(indexLatitude),
            longitude = if (cursor.isNull(indexLongitude)) null else cursor.getDouble(indexLongitude)
        )
    }
}