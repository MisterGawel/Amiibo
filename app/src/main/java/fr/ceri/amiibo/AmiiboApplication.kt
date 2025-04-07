package fr.ceri.amiibo

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration
import fr.ceri.amiibo.data.model.AmiiboQuestion
import fr.ceri.amiibo.data.realm.AmiiboRealm
import fr.ceri.amiibo.ui.GameActivity
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

    fun getRandomAmiiboQuestion(
        excludedIds: Set<String>,
        forcedType: QuestionType? = null
    ): AmiiboQuestion? {
        val realm = Realm.getDefaultInstance()
        val all = realm.where(AmiiboRealm::class.java).findAll()
            .filterNot { excludedIds.contains(it.id) }

        if (all.isEmpty()) return null

        val amiibo = all.random()

        val type = forcedType ?: if (Math.random() < 0.5) QuestionType.NAME else QuestionType.SERIES

        val correctAnswer = when (type) {
            QuestionType.NAME -> amiibo.name
            QuestionType.SERIES -> amiibo.gameSeries
        }

        val allAnswers = all.map {
            when (type) {
                QuestionType.NAME -> it.name
                QuestionType.SERIES -> it.gameSeries
            }
        }.distinct().shuffled().filter { it != correctAnswer }.take(2).toMutableList()

        allAnswers.add((0..allAnswers.size).random(), correctAnswer)

        return AmiiboQuestion(
            imageUrl = amiibo.image,
            choices = allAnswers,
            correctAnswerIndex = allAnswers.indexOf(correctAnswer),
            questionType = type
        )
    }

}