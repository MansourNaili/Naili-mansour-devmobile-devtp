package com.example.contactsapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))
        title = "Mes contacts"

        findViewById<MaterialButton>(R.id.btnRetourGlobal).setOnClickListener {
            onSupportNavigateUp()
        }

        supportFragmentManager.addOnBackStackChangedListener { mettreAJourBoutonRetour() }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ListeContactsFragment())
                .commit()
        }
        mettreAJourBoutonRetour()
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            return true
        }
        return false
    }

    private fun mettreAJourBoutonRetour() {
        val visible = supportFragmentManager.backStackEntryCount > 0
        findViewById<MaterialButton>(R.id.btnRetourGlobal).visibility =
            if (visible) View.VISIBLE else View.GONE
        // Le retour est désormais accessible en bas de l'écran, plus facile à toucher.
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
}
