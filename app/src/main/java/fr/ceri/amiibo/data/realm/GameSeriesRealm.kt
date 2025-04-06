package fr.ceri.amiibo.data.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class GameSeriesRealm(
    @PrimaryKey
    var name: String = ""
) : RealmObject()