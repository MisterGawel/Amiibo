package fr.ceri.amiibo.data.model

import fr.ceri.amiibo.ui.GameActivity

//* AmiiboQuestion est une question de type "choix multiple" qui contient
//*cc l'image de l'amiibo, le type de question (nom ou série), les choix possibles et l'index de la bonne réponse
data class AmiiboQuestion(
    val imageUrl: String,
    val questionType: GameActivity.QuestionType,
    val choices: List<String>,
    val correctAnswerIndex: Int
)