package com.example.contactsapp

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.contactsapp.data.Contact
import kotlinx.coroutines.launch
import java.io.File

class DetailContactFragment : Fragment(R.layout.fragment_detail_contact) {

    private val viewModel: ContactViewModel by activityViewModels()
    private val preferencesManager by lazy { PreferencesManager(requireContext()) }

    private lateinit var imgPhotoDetail: ImageView
    private lateinit var etNom: EditText
    private lateinit var etPrenom: EditText
    private lateinit var etTelephone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText

    private var contactId: Long = 0L
    private var estNouveau = false
    private var contactInitial: Contact? = null
    private var donneesChargees = false
    private var envoiEnCours = false
    private var photoUri: String? = null
    private var cameraFile: File? = null

    companion object {
        const val ARG_CONTACT_ID = "contactId"
        const val ETAT_PHOTO = "photoUri"
        const val ETAT_CAMERA = "cameraPath"
        const val ETAT_CHARGES = "donneesChargees"

        fun newInstance(contactId: Long = 0L): DetailContactFragment {
            return DetailContactFragment().apply {
                arguments = Bundle().apply { putLong(ARG_CONTACT_ID, contactId) }
            }
        }
    }

    private val galerieLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) {
                Toast.makeText(requireContext(), "Aucune photo sélectionnée", Toast.LENGTH_SHORT).show()
            } else {
                val fichier = copierImage(uri)
                if (fichier != null) {
                    reduireImage(fichier)
                    photoUri = fichier.absolutePath
                    afficherPhoto()
                } else {
                    Toast.makeText(requireContext(), "Impossible de charger l'image", Toast.LENGTH_LONG).show()
                }
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { succes ->
            val fichier = cameraFile
            if (succes && fichier != null && fichier.length() > 0) {
                reduireImage(fichier)
                photoUri = fichier.absolutePath
                afficherPhoto()
            } else {
                fichier?.delete()
                Toast.makeText(requireContext(), "Prise de photo annulée", Toast.LENGTH_SHORT).show()
            }
            cameraFile = null
        }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { accordee ->
            if (accordee) {
                lancerCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permission caméra refusée : impossible de prendre une photo",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactId = requireArguments().getLong(ARG_CONTACT_ID, 0L)
        estNouveau = contactId == 0L

        if (savedInstanceState != null) {
            photoUri = savedInstanceState.getString(ETAT_PHOTO)
            cameraFile = savedInstanceState.getString(ETAT_CAMERA)?.let { File(it) }
            donneesChargees = savedInstanceState.getBoolean(ETAT_CHARGES, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imgPhotoDetail = view.findViewById(R.id.imgPhotoDetail)
        etNom = view.findViewById(R.id.etNom)
        etPrenom = view.findViewById(R.id.etPrenom)
        etTelephone = view.findViewById(R.id.etTelephone)
        etEmail = view.findViewById(R.id.etEmail)
        etLatitude = view.findViewById(R.id.etLatitude)
        etLongitude = view.findViewById(R.id.etLongitude)

        view.findViewById<Button>(R.id.btnAppeler).setOnClickListener { appeler() }
        view.findViewById<Button>(R.id.btnEmail).setOnClickListener { envoyerEmail() }
        view.findViewById<Button>(R.id.btnVoirCarte).setOnClickListener { voirSurCarte() }
        view.findViewById<Button>(R.id.btnGalerie).setOnClickListener { ouvrirGalerie() }
        view.findViewById<Button>(R.id.btnCamera).setOnClickListener { ouvrirCamera() }
        view.findViewById<Button>(R.id.btnEnregistrer).setOnClickListener { enregistrer() }
        view.findViewById<Button>(R.id.btnAnnuler).setOnClickListener { quitter() }
        view.findViewById<Button>(R.id.btnSupprimer).apply {
            visibility = if (estNouveau) View.GONE else View.VISIBLE
            setOnClickListener { demanderSuppression() }
        }

        appliquerPreferences()
        afficherPhoto()
        observerOperations()

        if (estNouveau) {
            if (!donneesChargees) {
                contactInitial = Contact(nom = "", prenom = "", telephone = "")
                donneesChargees = true
            }
            return
        }

        if (donneesChargees) {
            // La rotation a déjà restauré les EditText : on ne les écrase pas.
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val contact = viewModel.getContactById(contactId)
            if (contact == null) {
                Toast.makeText(requireContext(), "Contact introuvable", Toast.LENGTH_LONG).show()
                parentFragmentManager.popBackStack()
                return@launch
            }
            contactInitial = contact
            photoUri = contact.photoUri
            remplirChamps(contact)
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().title = if (estNouveau) "Nouveau contact" else "Détail du contact"
        appliquerPreferences()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ETAT_PHOTO, photoUri)
        outState.putString(ETAT_CAMERA, cameraFile?.absolutePath)
        outState.putBoolean(ETAT_CHARGES, donneesChargees)
    }

    private fun observerOperations() {
        viewModel.operation.observe(viewLifecycleOwner) { event ->
            val resultat = event.consommer() ?: return@observe
            envoiEnCours = false
            when (resultat) {
                is OperationContact.Enregistre -> {
                    val message = if (resultat.creation) "Contact créé" else "Contact enregistré"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    quitter()
                }
                is OperationContact.Supprime -> {
                    Toast.makeText(requireContext(), "Contact supprimé", Toast.LENGTH_SHORT).show()
                    quitter()
                }
                is OperationContact.Echec -> {
                    Toast.makeText(requireContext(), resultat.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun appliquerPreferences() {
        val preferences = preferencesManager.lire()
        listOf(etNom, etPrenom, etTelephone).forEach {
            it.setTextSize(TypedValue.COMPLEX_UNIT_SP, preferences.tailleSp)
        }
        etNom.setTextColor(preferences.couleurTexte)
        etPrenom.setTextColor(preferences.couleurTexte)
    }

    private fun remplirChamps(contact: Contact) {
        etNom.setText(contact.nom)
        etPrenom.setText(contact.prenom)
        etTelephone.setText(contact.telephone)
        etEmail.setText(contact.email.orEmpty())
        etLatitude.setText(contact.latitude?.toString().orEmpty())
        etLongitude.setText(contact.longitude?.toString().orEmpty())
        donneesChargees = true
        afficherPhoto()
    }

    private fun afficherPhoto() {
        if (photoUri.isNullOrBlank()) {
            imgPhotoDetail.setImageResource(R.drawable.ic_person)
            return
        }
        runCatching { Uri.fromFile(File(photoUri!!)) }
            .onSuccess { imgPhotoDetail.setImageURI(it) }
            .onFailure { imgPhotoDetail.setImageResource(R.drawable.ic_person) }
    }

    private fun masquerClavier() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun quitter() {
        masquerClavier()
        parentFragmentManager.popBackStack()
    }

    private fun demanderSuppression() {
        if (estNouveau) return
        val contact = Contact(
            id = contactId,
            nom = etNom.text.toString(),
            prenom = etPrenom.text.toString(),
            telephone = etTelephone.text.toString(),
            photoUri = photoUri
        )
        AlertDialog.Builder(requireContext())
            .setTitle("Supprimer le contact")
            .setMessage("Voulez-vous vraiment supprimer ce contact ?")
            .setNegativeButton("Annuler", null)
            .setPositiveButton("Supprimer") { _, _ -> viewModel.supprimer(contact) }
            .show()
    }

    private fun voirSurCarte() {
        masquerClavier()
        val nom = "${etNom.text.toString().trim()} ${etPrenom.text.toString().trim()}".trim()
        val carte = CarteContactFragment.newInstance(
            contactId,
            if (nom.isEmpty()) "Contact" else nom,
            etLatitude.text.toString(),
            etLongitude.text.toString()
        )
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, carte)
            .addToBackStack(null)
            .commit()
    }

    private fun appeler() {
        val numero = etTelephone.text.toString().trim().replace(" ", "")
        if (numero.isEmpty()) {
            etTelephone.error = "Aucun numéro à appeler"
            etTelephone.requestFocus()
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

    private fun envoyerEmail() {
        val adresse = etEmail.text.toString().trim()
        if (adresse.isEmpty()) {
            etEmail.error = "Aucune adresse e-mail"
            etEmail.requestFocus()
            return
        }
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$adresse"))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                "Aucune application de messagerie disponible",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun ouvrirGalerie() {
        try {
            galerieLauncher.launch("image/*")
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                "Aucune application de galerie disponible",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun ouvrirCamera() {
        if (!requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(
                requireContext(),
                "Aucune caméra disponible sur cet appareil",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            lancerCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun lancerCamera() {
        val fichier = File(
            requireContext().filesDir,
            "contact_${contactId}_${System.currentTimeMillis()}.jpg"
        )
        cameraFile = fichier
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            fichier
        )
        try {
            cameraLauncher.launch(uri)
        } catch (e: ActivityNotFoundException) {
            cameraFile = null
            Toast.makeText(
                requireContext(),
                "Aucune application caméra disponible",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun copierImage(source: Uri): File? {
        return try {
            val fichier = File(
                requireContext().filesDir,
                "contact_${contactId}_${System.currentTimeMillis()}.jpg"
            )
            val entree = requireContext().contentResolver.openInputStream(source) ?: return null
            entree.use { input ->
                fichier.outputStream().use { sortie -> input.copyTo(sortie) }
            }
            fichier
        } catch (e: Exception) {
            null
        }
    }

    private fun reduireImage(fichier: File, tailleMax: Int = 1024) {
        try {
            val chemin = fichier.absolutePath
            val bornes = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(chemin, bornes)

            var echantillon = 1
            while (bornes.outWidth / echantillon > tailleMax * 2 ||
                bornes.outHeight / echantillon > tailleMax * 2
            ) {
                echantillon *= 2
            }

            val options = BitmapFactory.Options().apply { inSampleSize = echantillon }
            var bitmap = BitmapFactory.decodeFile(chemin, options) ?: return

            val orientation = ExifInterface(chemin).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            val angle = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            if (angle != 0f) {
                val matrice = Matrix().apply { postRotate(angle) }
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrice, true)
            }

            val ratio = minOf(1f, tailleMax.toFloat() / maxOf(bitmap.width, bitmap.height))
            if (ratio < 1f) {
                bitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * ratio).toInt(),
                    (bitmap.height * ratio).toInt(),
                    true
                )
            }

            fichier.outputStream().use { sortie ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, sortie)
            }
        } catch (e: Exception) {
            // Photo conservée telle quelle si le redimensionnement échoue.
        }
    }

    private fun enregistrer() {
        if (envoiEnCours) return
        val contact = construireContact() ?: return
        envoiEnCours = true
        viewModel.enregistrer(contact, estNouveau)
    }

    /** Valide les champs puis construit le Contact à écrire en base. */
    private fun construireContact(): Contact? {
        val nom = etNom.text.toString().trim()
        val prenom = etPrenom.text.toString().trim()
        val telephone = etTelephone.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val latitudeTexte = etLatitude.text.toString().trim()
        val longitudeTexte = etLongitude.text.toString().trim()

        if (nom.isEmpty()) {
            etNom.error = "Le nom est obligatoire"
            etNom.requestFocus()
            return null
        }
        if (prenom.isEmpty()) {
            etPrenom.error = "Le prénom est obligatoire"
            etPrenom.requestFocus()
            return null
        }
        if (telephone.isEmpty()) {
            etTelephone.error = "Le téléphone est obligatoire"
            etTelephone.requestFocus()
            return null
        }
        if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Adresse e-mail invalide"
            etEmail.requestFocus()
            return null
        }

        var latitude: Double? = null
        var longitude: Double? = null
        if (latitudeTexte.isNotEmpty() || longitudeTexte.isNotEmpty()) {
            if (latitudeTexte.isEmpty()) {
                etLatitude.error = "Renseignez aussi la latitude"
                etLatitude.requestFocus()
                return null
            }
            if (longitudeTexte.isEmpty()) {
                etLongitude.error = "Renseignez aussi la longitude"
                etLongitude.requestFocus()
                return null
            }
            Coordonnees.erreurLatitude(latitudeTexte)?.let {
                etLatitude.error = it
                etLatitude.requestFocus()
                return null
            }
            Coordonnees.erreurLongitude(longitudeTexte)?.let {
                etLongitude.error = it
                etLongitude.requestFocus()
                return null
            }
            latitude = Coordonnees.versDouble(latitudeTexte)
            longitude = Coordonnees.versDouble(longitudeTexte)
        }

        return Contact(
            id = if (estNouveau) 0L else contactId,
            nom = nom,
            prenom = prenom,
            telephone = telephone,
            email = email.ifEmpty { null },
            photoUri = photoUri,
            latitude = latitude,
            longitude = longitude
        )
    }
}
