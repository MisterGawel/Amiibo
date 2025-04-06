package fr.ceri.amiibo.data.realm

import io.realm.RealmObject

open class UserSettingsRealm : RealmObject() {
    var bestScore: Int = 0
    var isMusicEnabled: Boolean = true
    var questionCount: Int = 10
}