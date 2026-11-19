package com.example.contactsapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CoordonneesTest {

    @Test
    fun versDouble_accepte_virgule_et_espaces() {
        assertEquals(33.8, Coordonnees.versDouble(" 33,8 ")!!, 0.0001)
    }

    @Test
    fun versDouble_refuse_un_texte() {
        assertNull(Coordonnees.versDouble("abc"))
    }

    @Test
    fun latitude_hors_limites_est_refusee() {
        assertNotNull(Coordonnees.erreurLatitude("91"))
    }

    @Test
    fun longitude_hors_limites_est_refusee() {
        assertNotNull(Coordonnees.erreurLongitude("-181"))
    }

    @Test
    fun coordonnees_valides_sont_acceptees() {
        assertNull(Coordonnees.erreurGlobale("33.8076", "10.8451"))
    }

    @Test
    fun coordonnees_absentes_donnent_un_message() {
        assertNotNull(Coordonnees.erreurGlobale("", ""))
    }

    @Test
    fun latitude_seule_donne_un_message() {
        assertNotNull(Coordonnees.erreurGlobale("33.8076", ""))
    }
}
