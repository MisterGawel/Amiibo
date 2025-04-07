package fr.ceri.amiibo.data.realm

import io.realm.RealmObject

/**
 * Représente les paramètres utilisateur dans la base de données Realm.
 *
 * @property bestScore Le meilleur score de l'utilisateur.
 * @property isMusicEnabled Indique si la musique est activée.
 * @property questionCount Le nombre de questions à poser.
 */
open class UserSettingsRealm : RealmObject() {
    var bestScore: Int = 0
    var isMusicEnabled: Boolean = true
    var questionCount: Int = 10
}