package fr.ceri.amiibo

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration
import fr.ceri.amiibo.data.model.AmiiboQuestion
import fr.ceri.amiibo.data.realm.AmiiboRealm
import fr.ceri.amiibo.ui.GameActivity.QuestionType

/**
 * Classe Application principale de l'app.
 * Elle initialise la base de données Realm dès le lancement de l'application.
 */
class AmiiboApplication : Application() {

    companion object {
        lateinit var realm: Realm
    }

    override fun onCreate() {
        super.onCreate()

        // Initialise Realm avec le contexte de l'application
        Realm.init(this)
        val config = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .name("amiibo.realm")
            .build()

        Realm.setDefaultConfiguration(config)
        realm = Realm.getInstance(config)
    }

    /**
     * Génère aléatoirement une question sur un amiibo.
     *
     * @param excludedIds Liste des ID d’amiibos à exclure (par ex. ceux déjà vus).
     * @param forcedType Permet de forcer le type de question (nom ou série), sinon tirage aléatoire.
     * @return Une question de type AmiiboQuestion ou null si aucun amiibo dispo.
     */
    fun getRandomAmiiboQuestion(
        excludedIds: Set<String>,
        forcedType: QuestionType? = null
    ): AmiiboQuestion? {
        val realm = Realm.getDefaultInstance()

        // Récupère tous les amiibos sauf ceux dont l'ID est dans la liste exclue
        val all = realm.where(AmiiboRealm::class.java).findAll()
            .filterNot { excludedIds.contains(it.id) }

        // Si aucun amiibo disponible, retourne null
        if (all.isEmpty()) return null

        // Sélectionne un amiibo au hasard dans la liste restante
        val amiibo = all.random()

        // Détermine le type de question (forcé ou tiré au sort)
        val type = forcedType ?: if (Math.random() < 0.5) QuestionType.NAME else QuestionType.SERIES

        // Détermine la bonne réponse selon le type de question
        val correctAnswer = when (type) {
            QuestionType.NAME -> amiibo.name
            QuestionType.SERIES -> amiibo.gameSeries
        }

        // Prépare les mauvaises réponses : on filtre, mélange et prend 2 réponses incorrectes
        val allAnswers = all.map {
            when (type) {
                QuestionType.NAME -> it.name
                QuestionType.SERIES -> it.gameSeries
            }
        }.distinct()                            // Évite les doublons
            .shuffled()                           // Mélange les éléments
            .filter { it != correctAnswer }       // Exclut la bonne réponse
            .take(2)                              // Prend deux mauvaises réponses
            .toMutableList()

        // Ajoute la bonne réponse à une position aléatoire
        allAnswers.add((0..allAnswers.size).random(), correctAnswer)

        return AmiiboQuestion(
            imageUrl = amiibo.image,
            choices = allAnswers,
            correctAnswerIndex = allAnswers.indexOf(correctAnswer),
            questionType = type
        )
    }

}
