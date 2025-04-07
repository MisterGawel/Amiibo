package fr.ceri.amiibo.data.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Représente un amiibo dans la base de données Realm.
 *
 * @property id L'identifiant unique de l'amiibo.
 * @property name Le nom de l'amiibo.
 * @property gameSeries La série de jeu à laquelle l'amiibo appartient.
 * @property image L'URL de l'image de l'amiibo.
 */
open class AmiiboRealm(
    @PrimaryKey
    var id: String = "",
    var name: String = "",
    var gameSeries: String = "",
    var image: String = ""
) : RealmObject()