# ContactsApp V4 — TP Android (SQLite, Préférences, Fragments, Carte)

Application Android en **Kotlin** avec interfaces **XML**. Elle fait évoluer la V3 : les contacts ne sont plus conservés en mémoire mais dans une base **SQLite native** (`contacts.db`). Trois préférences utilisateur personnalisent l'affichage et la liste.

## Fonctionnalités

### Conservées de la V3
- `MainActivity` unique avec `FragmentContainerView` (`fragmentContainer`) et navigation `replace()` + `addToBackStack()`.
- `ListeContactsFragment` : `RecyclerView` (photo, nom, prénom, téléphone), filtrage en temps réel, message « aucun résultat ».
- `DetailContactFragment` : consultation, modification, validation, Annuler, Appeler (`ACTION_DIAL`), E-mail (`ACTION_SENDTO`), Galerie, Caméra, Voir sur la carte.
- `CarteContactFragment` : marqueur au nom du contact, messages explicites si les coordonnées sont absentes ou invalides.
- Identification par `id` (jamais par position dans la liste).

### Nouveautés V4
- **SQLite natif** : `ContactContract`, `ContactDbHelper`, `ContactDao`, `Contact` dans le paquet `data`.
- **CRUD persistant** : bouton flottant « + » (Create), lecture depuis la base, modification, suppression avec `AlertDialog`.
- **6 contacts de démonstration** insérés une seule fois (dont 2 sans GPS).
- **Préférences** (`SharedPreferences`) : taille de police, couleur du texte, bouton Appeler dans la liste.
- **Application immédiate** des préférences au retour de l'écran Paramètres.
- **Threads** : toutes les opérations base passent par `viewModelScope` + `withContext(Dispatchers.IO)`.
- **Photos** : copie dans `filesDir`, chemin absolu enregistré en base.

## Architecture

| Composant | Rôle |
|---|---|
| `MainActivity` | Héberge les fragments, la barre d'application et le bouton Retour global en bas. |
| `ListeContactsFragment` | Liste, filtrage SQL, FAB « + », bouton Paramètres sous le FAB, bouton Appeler par ligne. |
| `DetailContactFragment` | Création / consultation / modification / suppression, actions V3. |
| `CarteContactFragment` | Carte osmdroid, nom et coordonnées reçus par `Bundle`. |
| `ParametresFragment` | Écran `PreferenceFragmentCompat` (`res/xml/preferences.xml`). |
| `ContactViewModel` | `LiveData` de la liste, appels DAO dans `viewModelScope`, événements CRUD. |
| `ContactDao` | Seul le SQL : `insert`, `update`, `delete`, `getAll`, `getById`, `search`. |
| `ContactDbHelper` | Singleton `SQLiteOpenHelper`, création/migration de `contacts.db`. |
| `PreferencesManager` | Lecture centralisée et conversion des préférences. |

## Schéma de la base

```sql
CREATE TABLE contacts (
    _id        INTEGER PRIMARY KEY AUTOINCREMENT,
    nom        TEXT NOT NULL,
    prenom     TEXT NOT NULL,
    telephone  TEXT NOT NULL,
    email      TEXT,
    photo_uri  TEXT,
    latitude   REAL,
    longitude  REAL
);
```

- `telephone` est en `TEXT` : il peut commencer par `0` ou `+216` et contenir des espaces.
- `latitude` / `longitude` sont `NULL` quand le contact n'a pas de position.
- Tri : `ORDER BY nom COLLATE NOCASE ASC, prenom COLLATE NOCASE ASC`.
- Filtrage : SQL `LIKE ... ESCAPE '\'` avec `selectionArgs` (aucune concaténation de saisie utilisateur).

## Carte

La V3 utilisait **osmdroid / OpenStreetMap**. Ce choix est conservé : aucune clé API, aucun compte de facturation, donc aucun secret à publier. Une connexion Internet est nécessaire pour charger les tuiles.

## Préférences

| Clé | Composant | Valeurs | Défaut |
|---|---|---|---|
| `taille_police` | `ListPreference` | 14 / 16 / 20 sp | 16 |
| `couleur_preferee` | `ListPreference` | #000000, #1565C0, #2E7D32, #C62828, #6A1B9A | #000000 |
| `appel_depuis_liste` | `SwitchPreferenceCompat` | true / false | false |

Le thème est volontairement figé en clair pour garantir la lisibilité de toutes les couleurs proposées.

## Lancer le projet

1. Ouvrir le dossier dans Android Studio.
2. Attendre la synchronisation Gradle.
3. Lancer sur un émulateur ou un téléphone (API 24 minimum, Internet pour la carte).
4. Vérifier avec **App Inspection → Database Inspector** (API 26+) le contenu de `contacts.db`.

Commandes utiles :

```bash
./gradlew.bat compileDebugKotlin
./gradlew.bat testDebugUnitTest
./gradlew.bat assembleDebug
```

## Livrables

- `compte-rendu/compte_rendu_V4.pdf` : version PDF prête à rendre.
- `captures/` : captures réelles de l'application (voir `captures/README.md`).
- `tools/inspecter_base.bat` : inspection rapide de `contacts.db` via `adb` + `sqlite3`.
- `.gitignore` : `local.properties` et dossiers de build exclus du dépôt.

## Auteur

Mansour — L3 DSI, ISET Djerba.
