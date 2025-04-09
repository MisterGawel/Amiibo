package fr.ceri.amiibo.data.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Représente un amiibo dans la base de données Realm.
 *
 * @property id Identifiant unique de l'amiibo. Sert de clé primaire dans Realm.
 * @property name Nom de l'amiibo (exemple : "Mario").
 * @property gameSeries Nom de la série de jeu à laquelle l'amiibo appartient (exemple : "Super Mario Bros.").
 * @property image URL de l'image représentant visuellement l'amiibo.
 */
open class AmiiboRealm(
    @PrimaryKey
    var id: String = "",           // Clé primaire utilisée pour identifier de manière unique un amiibo dans la base.
    var name: String = "",         // Nom affiché de l’amiibo.
    var gameSeries: String = "",   // Série de jeux liée à l’amiibo.
    var image: String = ""         // URL pointant vers l’image de l’amiibo.
) : RealmObject()
