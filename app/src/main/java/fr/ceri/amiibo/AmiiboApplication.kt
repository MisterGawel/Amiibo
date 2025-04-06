package fr.ceri.amiibo

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration
import fr.ceri.amiibo.data.model.AmiiboQuestion
import fr.ceri.amiibo.data.realm.AmiiboRealm
import fr.ceri.amiibo.ui.GameActivity.QuestionType
import kotlin.random.Random

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


    fun getRandomAmiiboQuestion(excludedIds: Set<String>): AmiiboQuestion? {
        val allAmiibos = realm.where(AmiiboRealm::class.java).findAll()

        val available = allAmiibos.filterNot { excludedIds.contains(it.id) }
        val amiibo = available.randomOrNull() ?: return null

        val questionType = if (Random.nextBoolean()) QuestionType.NAME else QuestionType.SERIES
        val correctValue = when (questionType) {
            QuestionType.NAME -> amiibo.name
            QuestionType.SERIES -> amiibo.gameSeries
        }

        val allValues = allAmiibos.map {
            when (questionType) {
                QuestionType.NAME -> it.name
                QuestionType.SERIES -> it.gameSeries
            }
        }.distinct().filterNot { it == correctValue }.shuffled()

        val choices = mutableListOf(correctValue)
        choices.addAll(allValues.take(2))

        val shuffled = choices.shuffled()
        val correctIndex = shuffled.indexOf(correctValue)

        return AmiiboQuestion(
            imageUrl = amiibo.image,
            questionType = questionType,
            choices = shuffled,
            correctAnswerIndex = correctIndex
        )
    }
}