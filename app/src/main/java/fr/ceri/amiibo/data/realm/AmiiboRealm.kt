package fr.ceri.amiibo.data.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class AmiiboRealm(
    @PrimaryKey
    var id: String = "",
    var name: String = "",
    var gameSeries: String = "",
    var image: String = ""
) : RealmObject()