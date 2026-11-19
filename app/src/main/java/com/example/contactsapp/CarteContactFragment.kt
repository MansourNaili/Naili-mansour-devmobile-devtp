package com.example.contactsapp

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File

/**
 * Carte conservée de la V3 (osmdroid/OpenStreetMap) : aucune clé API à publier.
 * Le nom et les coordonnées sont transmis par Bundle, donc aucun accès à la base ici.
 */
class CarteContactFragment : Fragment(R.layout.fragment_carte_contact) {

    private var mapView: MapView? = null

    companion object {
        const val ARG_CONTACT_ID = "arg_contact_id"
        const val ARG_NOM = "arg_nom"
        const val ARG_LATITUDE = "arg_latitude"
        const val ARG_LONGITUDE = "arg_longitude"

        fun newInstance(
            contactId: Long,
            nom: String,
            latitude: String,
            longitude: String
        ): CarteContactFragment {
            return CarteContactFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_CONTACT_ID, contactId)
                    putString(ARG_NOM, nom)
                    putString(ARG_LATITUDE, latitude)
                    putString(ARG_LONGITUDE, longitude)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val contexte = requireContext().applicationContext
        val config = Configuration.getInstance()
        config.load(contexte, contexte.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        config.userAgentValue = contexte.packageName

        val dossier = File(contexte.cacheDir, "osmdroid")
        config.osmdroidBasePath = dossier
        config.osmdroidTileCache = File(dossier, "tiles")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvMessage = view.findViewById<TextView>(R.id.tvMessageCarte)
        val carte = view.findViewById<MapView>(R.id.map)
        mapView = carte

        view.findViewById<Button>(R.id.btnRetourCarte).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val args = requireArguments()
        val latitudeTexte = args.getString(ARG_LATITUDE).orEmpty()
        val longitudeTexte = args.getString(ARG_LONGITUDE).orEmpty()
        val nom = args.getString(ARG_NOM)?.takeIf { it.isNotBlank() } ?: "Contact"

        val erreur = Coordonnees.erreurGlobale(latitudeTexte, longitudeTexte)
        if (erreur != null) {
            carte.visibility = View.GONE
            tvMessage.text = erreur
            tvMessage.visibility = View.VISIBLE
            return
        }

        val latitude = Coordonnees.versDouble(latitudeTexte) ?: return
        val longitude = Coordonnees.versDouble(longitudeTexte) ?: return
        val position = GeoPoint(latitude, longitude)

        carte.setTileSource(TileSourceFactory.MAPNIK)
        carte.setMultiTouchControls(true)
        carte.controller.setZoom(15.0)
        carte.controller.setCenter(position)

        val marqueur = Marker(carte)
        marqueur.position = position
        marqueur.title = nom
        marqueur.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        carte.overlays.add(marqueur)
        marqueur.showInfoWindow()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().title = "Carte du contact"
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onDestroyView() {
        mapView?.onDetach()
        mapView = null
        super.onDestroyView()
    }
}
