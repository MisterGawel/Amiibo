package fr.ceri.amiibo.data.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Représente une série de jeux dans la base de données Realm.
 *
 * @property name Le nom de la série de jeux.
 */
open class GameSeriesRealm(
    @PrimaryKey
    var name: String = ""
) : RealmObject()