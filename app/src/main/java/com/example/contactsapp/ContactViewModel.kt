package com.example.contactsapp

import android.app.Application
import android.database.SQLException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.contactsapp.data.Contact
import com.example.contactsapp.data.ContactDao
import com.example.contactsapp.data.ContactDbHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** Résultat d'une écriture, consommé une seule fois par le fragment. */
sealed class OperationContact {
    data class Enregistre(val id: Long, val creation: Boolean) : OperationContact()
    data class Supprime(val id: Long) : OperationContact()
    data class Echec(val message: String) : OperationContact()
}

class Event<out T>(private val contenu: T) {
    private var consomme = false

    fun consommer(): T? = if (consomme) null else {
        consomme = true
        contenu
    }
}

class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ContactDao(ContactDbHelper.getInstance(application.applicationContext))

    private var requete = ""
    private var tacheRecherche: Job? = null

    private val _contacts = MutableLiveData<List<Contact>>(emptyList())
    val contacts: LiveData<List<Contact>> = _contacts

    private val _operation = MutableLiveData<Event<OperationContact>>()
    val operation: LiveData<Event<OperationContact>> = _operation

    init {
        chargerContacts()
    }

    /** Recharge la liste (filtrée si une recherche est active) hors du thread principal. */
    fun chargerContacts() {
        tacheRecherche?.cancel()
        tacheRecherche = viewModelScope.launch {
            val liste = withContext(Dispatchers.IO) {
                try {
                    if (requete.isBlank()) dao.getAll() else dao.search(requete)
                } catch (e: SQLException) {
                    emptyList<Contact>()
                }
            }
            _contacts.value = liste
        }
    }

    /** Filtrage SQL paramétré (voir ContactDao.search). */
    fun rechercher(texte: String) {
        val nouveau = texte.trim()
        if (nouveau == requete) return
        requete = nouveau
        chargerContacts()
    }

    suspend fun getContactById(id: Long): Contact? = withContext(Dispatchers.IO) {
        if (id <= 0L) {
            null
        } else {
            try {
                dao.getById(id)
            } catch (e: SQLException) {
                null
            }
        }
    }

    fun enregistrer(contact: Contact, creation: Boolean) {
        viewModelScope.launch {
            val id = withContext(Dispatchers.IO) {
                try {
                    if (creation) {
                        dao.insert(contact.copy(id = 0L))
                    } else if (dao.update(contact) > 0) {
                        contact.id
                    } else {
                        -1L
                    }
                } catch (e: SQLException) {
                    -1L
                }
            }

            if (id > 0) {
                chargerContacts()
                _operation.value = Event(OperationContact.Enregistre(id, creation))
            } else {
                _operation.value = Event(OperationContact.Echec("Impossible d'enregistrer le contact"))
            }
        }
    }

    fun supprimer(contact: Contact) {
        viewModelScope.launch {
            val lignes = withContext(Dispatchers.IO) {
                try {
                    val nombre = dao.delete(contact.id)
                    if (nombre > 0) supprimerPhoto(contact.photoUri)
                    nombre
                } catch (e: SQLException) {
                    0
                }
            }

            if (lignes > 0) {
                chargerContacts()
                _operation.value = Event(OperationContact.Supprime(contact.id))
            } else {
                _operation.value = Event(OperationContact.Echec("Le contact n'existe plus"))
            }
        }
    }

    /** Ne supprime que les fichiers appartenant au stockage interne de l'application. */
    private fun supprimerPhoto(chemin: String?) {
        if (chemin.isNullOrBlank()) return
        val fichier = File(chemin)
        val dossierApp = getApplication<Application>().filesDir.absolutePath
        if (fichier.absolutePath.startsWith(dossierApp)) {
            fichier.delete()
        }
    }
}
