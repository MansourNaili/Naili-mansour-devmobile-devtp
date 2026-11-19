package com.example.contactsapp

import android.content.Context
import android.graphics.Color
import androidx.preference.PreferenceManager

/** Valeurs déjà converties, prêtes à être appliquées aux vues. */
data class PreferencesAffichage(
    val tailleSp: Float,
    val couleurTexte: Int,
    val appelDepuisListe: Boolean
)

/** Lecture centrale des SharedPreferences (aucune clé en dur dans les fragments/adapters). */
class PreferencesManager(context: Context) {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    fun lire(): PreferencesAffichage {
        val taille = prefs.getString(KEY_TAILLE, DEF_TAILLE)?.toFloatOrNull() ?: 16f
        val couleur = prefs.getString(KEY_COULEUR, DEF_COULEUR) ?: DEF_COULEUR
        val couleurTexte = runCatching { Color.parseColor(couleur) }.getOrDefault(Color.BLACK)
        val appel = prefs.getBoolean(KEY_APPEL, false)
        return PreferencesAffichage(taille, couleurTexte, appel)
    }

    companion object {
        const val KEY_TAILLE = "taille_police"
        const val KEY_COULEUR = "couleur_preferee"
        const val KEY_APPEL = "appel_depuis_liste"
        const val DEF_TAILLE = "16"
        const val DEF_COULEUR = "#000000"
    }
}
