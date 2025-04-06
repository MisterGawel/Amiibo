package fr.ceri.amiibo

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class AmiiboApplication : Application() {

    companion object {
        lateinit var realm: Realm
    }

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)

        val config = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .name("amiibo.realm")
            .build()

        Realm.setDefaultConfiguration(config)
        realm = Realm.getInstance(config)
    }
}