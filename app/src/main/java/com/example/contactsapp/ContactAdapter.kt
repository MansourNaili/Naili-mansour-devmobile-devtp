package com.example.contactsapp

import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.contactsapp.data.Contact
import java.io.File

class ContactAdapter(
    private var contacts: List<Contact>,
    private val onItemClick: (Contact) -> Unit,
    private val onAppel: (Contact) -> Unit,
    private var afficherAppel: Boolean,
    private var tailleSp: Float,
    private var couleurTexte: Int
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPhoto: ImageView = itemView.findViewById(R.id.imgPhoto)
        val tvNomPrenom: TextView = itemView.findViewById(R.id.tvNomPrenom)
        val tvTelephone: TextView = itemView.findViewById(R.id.tvTelephone)
        val btnAppeler: ImageButton = itemView.findViewById(R.id.btnAppelerItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun getItemCount(): Int = contacts.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]

        holder.tvNomPrenom.text = "${contact.nom} ${contact.prenom}"
        holder.tvTelephone.text = contact.telephone

        holder.tvNomPrenom.setTextSize(TypedValue.COMPLEX_UNIT_SP, tailleSp)
        holder.tvTelephone.setTextSize(TypedValue.COMPLEX_UNIT_SP, tailleSp)
        holder.tvNomPrenom.setTextColor(couleurTexte)

        afficherPhoto(holder.imgPhoto, contact.photoUri)

        holder.btnAppeler.visibility = if (afficherAppel) View.VISIBLE else View.GONE
        holder.btnAppeler.setOnClickListener { onAppel(contact) }

        holder.itemView.setOnClickListener { onItemClick(contact) }
    }

    fun updateList(newList: List<Contact>) {
        contacts = newList
        notifyDataSetChanged()
    }

    fun appliquerPreferences(preferences: PreferencesAffichage) {
        afficherAppel = preferences.appelDepuisListe
        tailleSp = preferences.tailleSp
        couleurTexte = preferences.couleurTexte
        notifyDataSetChanged()
    }

    private fun afficherPhoto(imageView: ImageView, chemin: String?) {
        if (chemin.isNullOrBlank()) {
            imageView.setImageResource(R.drawable.ic_person)
            return
        }
        runCatching { Uri.fromFile(File(chemin)) }
            .onSuccess { imageView.setImageURI(it) }
            .onFailure { imageView.setImageResource(R.drawable.ic_person) }
    }
}
