# Captures réelles — ContactsApp V4

Toutes les images ont été prises sur l'émulateur Pixel 6 avec l'APK final (`app-debug.apk`). Elles sont référencées dans `compte-rendu/compte_rendu_V4.md` et intégrées automatiquement au PDF par `compte-rendu/generate_pdf.py`.

| Fichier | Contenu |
|---|---|
| `00_liste_finale.png` | État final de la liste (préférences par défaut) |
| `00_barre_haut.png` | Essai de barre haute (non retenu, UI remise à l'état initial) |
| `00_emulateur_liste.png` | Liste dans l'émulateur |
| `00_liste_bouton_parametres.png` | Essai de bouton Paramètres en bas (non retenu) |
| `01_liste_premier_lancement.png` | Les 6 contacts de démonstration |
| `01_creation_oneil.png` / `02_creation_contact.png` | Création d'un contact avec apostrophe |
| `03_modification_contact.png` | Modification d'un contact |
| `04_confirmation_suppression.png` | `AlertDialog` de suppression |
| `05_liste_apres_relance.png` | Persistance après fermeture complète |
| `06_database_inspector.png` | Table `contacts` dans Android Studio (terminal + `sqlite3`) |
| `07_parametres.png` | Les trois préférences |
| `08_police_petite.png` / `09_police_grande.png` | Comparaison des tailles 14 et 20 sp |
| `08_police_grande_bleu_appel.png` | Combinaison taille 20 sp + bleu + appel activé |
| `10_couleur_bleu.png` / `11_couleur_rouge.png` | Comparaison des couleurs |
| `12_appel_liste_active.png` | Boutons Appeler visibles sur chaque ligne |
| `13_composeur_depuis_liste.png` | Composeur ouvert depuis la liste, sans ouvrir la fiche |
| `14_carte_marqueur.png` | Carte osmdroid avec marqueur au nom du contact |
| `15_rotation.png` | Saisie conservée après rotation |
| `17_liste_boutons_bas.png` | Bouton Paramètres sous le FAB, accessible en bas |
| `18_parametres_retour_bas.png` | Bouton Retour en bas de l'écran Paramètres |
| `19_detail_retour_bas.png` | Bouton Retour en bas de la fiche détaillée |
| `aucun-resultat-filtre.png` | Message « aucun résultat » pour un filtre sans correspondance |
| `autorisation-de-camera.png` | Demande de permission caméra |
| `cas-aucune-position.png` | Message quand le contact n'a pas de coordonnées |
| `champs-vide.png` | Validation des champs obligatoires |
| `composeur-numero.png` | `ACTION_DIAL` depuis la fiche |
| `contact-avec-photo.png` | Photo enregistrée et affichée |
| `detail-contact.png` | Fiche détaillée |
| `email.png` | Envoi d'e-mail (`ACTION_SENDTO`) |
| `filtré-par-nom.png`, `filtrage-par-numero.png` | Filtres SQL |
| `galerie.png` | Choix d'une photo dans la galerie |
| `liste-apres-modification.png`, `liste-complete.png`, `liste-contact.png` | Vues de la liste |
| `message-d'erreur.png` | Coordonnées invalides |

## Capture 6 — Inspection de la base

Deux méthodes équivalentes :

1. Dans Android Studio : **View → Tool Windows → App Inspection**, onglet **Database Inspector**, sélectionner `com.example.contactsapp` → `contacts.db` → table `contacts`.
2. Dans le terminal Android Studio, exécuter `tools/inspecter_base.bat` : le script interroge la base réelle avec `sqlite3` et affiche la table `contacts`.

La capture livrée correspond à la méthode 2 (terminal maximisé), car l'émulateur et l'inspecteur graphique n'étaient pas toujours disponibles ensemble.
