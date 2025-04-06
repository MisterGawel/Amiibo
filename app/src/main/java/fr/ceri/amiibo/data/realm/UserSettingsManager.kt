package fr.ceri.amiibo.data.realm

import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UserSettingsManager {

    //* Instance de UserSettingsManager
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

    suspend fun getBestScore(): Int = withContext(Dispatchers.IO) {
        Realm.getDefaultInstance().use { realm ->
            getSettings(realm).bestScore
        }
    }

    suspend fun setBestScore(value: Int) = withContext(Dispatchers.IO) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                getSettings(it).bestScore = value
            }
        }
    }

    suspend fun isMusicEnabled(): Boolean = withContext(Dispatchers.IO) {
        Realm.getDefaultInstance().use { realm ->
            getSettings(realm).isMusicEnabled
        }
    }

    suspend fun setMusicEnabled(value: Boolean) = withContext(Dispatchers.IO) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                getSettings(it).isMusicEnabled = value
            }
        }
    }

    suspend fun getQuestionCount(): Int = withContext(Dispatchers.IO) {
        Realm.getDefaultInstance().use { realm ->
            getSettings(realm).questionCount
        }
    }

    suspend fun setQuestionCount(value: Int) = withContext(Dispatchers.IO) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                getSettings(it).questionCount = value
            }
        }
    }

    //* Fonction nettoyer la base de données au cas ou
    suspend fun clearAll() = withContext(Dispatchers.IO) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                it.delete(UserSettingsRealm::class.java)
                it.createObject(UserSettingsRealm::class.java)
            }
        }
    }
}