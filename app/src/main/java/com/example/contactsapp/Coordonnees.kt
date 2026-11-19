package com.example.contactsapp

object Coordonnees {

    private val formatNombre = Regex("-?\\d+(\\.\\d+)?")

    fun versDouble(texte: String): Double? {
        val propre = texte.trim().replace(',', '.')
        if (!formatNombre.matches(propre)) return null
        return propre.toDouble()
    }

    fun erreurLatitude(texte: String): String? {
        val valeur = versDouble(texte) ?: return "La latitude doit être un nombre"
        if (valeur < -90.0 || valeur > 90.0) {
            return "La latitude doit être comprise entre -90 et 90"
        }
        return null
    }

    fun erreurLongitude(texte: String): String? {
        val valeur = versDouble(texte) ?: return "La longitude doit être un nombre"
        if (valeur < -180.0 || valeur > 180.0) {
            return "La longitude doit être comprise entre -180 et 180"
        }
        return null
    }

    fun erreurGlobale(latitude: String, longitude: String): String? {
        val latitudeVide = latitude.isBlank()
        val longitudeVide = longitude.isBlank()
        if (latitudeVide && longitudeVide) return "Aucune position GPS n'est définie pour ce contact"
        if (latitudeVide) return "La latitude est absente"
        if (longitudeVide) return "La longitude est absente"
        return erreurLatitude(latitude) ?: erreurLongitude(longitude)
    }
}