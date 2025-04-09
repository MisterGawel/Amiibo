package fr.ceri.amiibo.data.model

import fr.ceri.amiibo.ui.GameActivity

// Classe de données représentant une question à choix multiples (QCM) utilisée dans le jeu.
// Elle sert à générer une question basée sur un amiibo
// Elle contient les éléments nécessaires à l'affichage et à la validation de la réponse.

data class AmiiboQuestion(
    val imageUrl: String,                      // URL de l'image de l'amiibo à afficher pour la question.
    val questionType: GameActivity.QuestionType, // Type de question (par exemple : deviner le nom ou la série de l'amiibo).
    val choices: List<String>,                 // Liste des choix de réponses proposés à l'utilisateur.
    val correctAnswerIndex: Int                // Index de la bonne réponse dans la liste des choix.
)
