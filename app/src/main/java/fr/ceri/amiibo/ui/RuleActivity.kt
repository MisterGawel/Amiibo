package fr.ceri.amiibo.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.ceri.amiibo.databinding.ActivityRuleBinding
import fr.ceri.amiibo.R

/**
 * Activité affichant les règles du jeu à l'utilisateur.
 * Elle stylise dynamiquement les textes à l'aide de couleurs, et propose un bouton de retour.
 */
class RuleActivity : AppCompatActivity() {

    private lateinit var ui: ActivityRuleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialisation du binding
        ui = ActivityRuleBinding.inflate(layoutInflater)
        setContentView(ui.root)

        // Application des styles aux titres et textes explicatifs
        styleTitle()
        styleSwipeTexts()

        // Définition des comportements des boutons
        setupListeners()
    }

    /**
     * Initialise les écouteurs d'événements sur les boutons de l'interface.
     */
    private fun setupListeners() {
        // Ferme l'activité quand l'utilisateur clique sur "J'ai compris"
        ui.btnUnderstood.setOnClickListener {
            finish()
        }

        // Ferme aussi l'activité si on clique sur l'icône de retour en bas
        ui.footerIconLeft.setOnClickListener {
            finish()
        }
    }

    /**
     * Met en forme le titre principal de l'écran en colorant une partie du texte.
     * Le premier morceau est coloré en rose (#C8007D).
     */
    private fun styleTitle() {
        val part1 = getString(R.string.rules_title_part1)
        val part2 = getString(R.string.rules_title_part2)
        val fullText = "$part1 $part2"

        val spannable = android.text.SpannableString(fullText)
        spannable.setSpan(
            android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#C8007D")),
            0,
            part1.length,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ui.tvTitle.text = spannable
    }

    /**
     * Met en forme les explications liées aux swipes
     * - à droite (nom de personnage) avec texte en rose,
     * - à gauche (série de jeu) avec texte en bleu.
     */
    private fun styleSwipeTexts() {
        // Texte de swipe vers la droite
        val rightPrefix = getString(R.string.swipe_right_prefix)
        val rightColor = getString(R.string.swipe_right_colored)
        val rightText = "$rightPrefix $rightColor"
        val spannableRight = android.text.SpannableString(rightText)
        spannableRight.setSpan(
            android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#C8007D")),
            rightText.indexOf(rightColor),
            rightText.length,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ui.swipeRight.text = spannableRight

        // Texte de swipe vers la gauche
        val leftPrefix = getString(R.string.swipe_left_prefix)
        val leftColor = getString(R.string.swipe_left_colored)
        val leftText = "$leftPrefix $leftColor"
        val spannableLeft = android.text.SpannableString(leftText)
        spannableLeft.setSpan(
            android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#00AAEB")),
            leftText.indexOf(leftColor),
            leftText.length,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ui.swipeLeft.text = spannableLeft
    }
}
