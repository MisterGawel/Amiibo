package fr.ceri.amiibo.utils

import fr.ceri.amiibo.data.realm.UserSettingsRealm
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Gestionnaire des paramètres utilisateur dans la base de données Realm.
 *
 * @property bestScore Le meilleur score de l'utilisateur.
 * @property isMusicEnabled Indique si la musique est activée.
 * @property questionCount Le nombre de questions à poser.
 */
object UserSettingsManager {

    /**
     * Récupère les paramètres utilisateur depuis la base Realm.
     * Si aucun objet n'existe encore, il est automatiquement créé.
     *
     * @param realm Instance de la base Realm utilisée pour la requête.
     * @return L'objet UserSettingsRealm contenant les préférences utilisateur.
     */
    private fun getSettings(realm: Realm): UserSettingsRealm {
        var settings = realm.where<UserSettingsRealm>().findFirst()
        if (settings == null) {
            realm.executeTransaction {
                it.createObject(UserSettingsRealm::class.java)
            }
            settings = realm.where<UserSettingsRealm>().findFirst()
        }
        return settings!!
    }

    /**
     * Récupère le meilleur score enregistré.
     */
    suspend fun getBestScore(): Int = withContext(Dispatchers.IO) {
        Realm.getDefaultInstance().use { realm ->
            getSettings(realm).bestScore
        }
    }

    /**
     * Définit le meilleur score enregistré.
     *
     * @param value Le nouveau meilleur score à enregistrer.
     */
    suspend fun setBestScore(value: Int) = withContext(Dispatchers.IO) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                getSettings(it).bestScore = value
            }
        }
    }

    /**
     * Vérifie si la musique est activée.
     *
     * @return true si la musique est activée, false sinon.
     */
    suspend fun isMusicEnabled(): Boolean = withContext(Dispatchers.IO) {
        Realm.getDefaultInstance().use { realm ->
            getSettings(realm).isMusicEnabled
        }
    }

    /**
     * Définit si la musique est activée.
     *
     * @param value true pour activer la musique, false pour la désactiver.
     */
    suspend fun setMusicEnabled(value: Boolean) = withContext(Dispatchers.IO) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                getSettings(it).isMusicEnabled = value
            }
        }
    }

    /**
     * Récupère le nombre de questions à poser.
     *
     * @return Le nombre de questions à poser.
     */
    suspend fun getQuestionCount(): Int = withContext(Dispatchers.IO) {
        Realm.getDefaultInstance().use { realm ->
            getSettings(realm).questionCount
        }
    }

    /**
     * Définit le nombre de questions à poser.
     *
     * @param value Le nouveau nombre de questions à poser.
     */
    suspend fun setQuestionCount(value: Int) = withContext(Dispatchers.IO) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                getSettings(it).questionCount = value
            }
        }
    }
}