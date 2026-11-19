package com.example.contactsapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contactsapp.data.Contact
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ListeContactsFragment : Fragment(R.layout.fragment_liste_contacts) {

    private val viewModel: ContactViewModel by activityViewModels()
    private val preferencesManager by lazy { PreferencesManager(requireContext()) }

    private lateinit var prefs: SharedPreferences
    private lateinit var adapter: ContactAdapter
    private lateinit var etRecherche: EditText
    private lateinit var tvAucunResultat: TextView

    private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        appliquerPreferences()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        etRecherche = view.findViewById(R.id.etRecherche)
        tvAucunResultat = view.findViewById(R.id.tvAucunResultat)
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerContacts)

        adapter = ContactAdapter(
            contacts = emptyList(),
            onItemClick = { ouvrirDetail(it) },
            onAppel = { appeler(it) },
            afficherAppel = false,
            tailleSp = 16f,
            couleurTexte = Color.BLACK
        )

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
        recycler.adapter = adapter

        view.findViewById<FloatingActionButton>(R.id.fabAjouter).setOnClickListener {
            ouvrirCreation()
        }

        view.findViewById<MaterialButton>(R.id.btnParametres).setOnClickListener {
            ouvrirParametres()
        }

        etRecherche.doOnTextChanged { texte, _, _, _ ->
            viewModel.rechercher(texte.toString())
        }

        viewModel.contacts.observe(viewLifecycleOwner) {
            afficherListe()
        }

        appliquerPreferences()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().title = "Mes contacts"
        viewModel.chargerContacts()
        appliquerPreferences()
        prefs.registerOnSharedPreferenceChangeListener(preferenceListener)
    }

    override fun onPause() {
        prefs.unregisterOnSharedPreferenceChangeListener(preferenceListener)
        super.onPause()
    }

    private fun appliquerPreferences() {
        if (::adapter.isInitialized) {
            adapter.appliquerPreferences(preferencesManager.lire())
        }
    }

    private fun afficherListe() {
        val resultat = viewModel.contacts.value.orEmpty()
        adapter.updateList(resultat)
        tvAucunResultat.visibility = if (resultat.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun ouvrirDetail(contact: Contact) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, DetailContactFragment.newInstance(contact.id))
            .addToBackStack(null)
            .commit()
    }

    private fun ouvrirCreation() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, DetailContactFragment.newInstance(0L))
            .addToBackStack(null)
            .commit()
    }

    private fun ouvrirParametres() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, ParametresFragment())
            .addToBackStack(null)
            .commit()
    }

    /** ACTION_DIAL : aucune permission, et le clic n'ouvre pas la fiche détaillée. */
    private fun appeler(contact: Contact) {
        val numero = contact.telephone.trim().replace(" ", "")
        if (numero.isEmpty()) {
            Toast.makeText(requireContext(), "Aucun numéro à appeler", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$numero"))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                "Aucune application de téléphone disponible",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
