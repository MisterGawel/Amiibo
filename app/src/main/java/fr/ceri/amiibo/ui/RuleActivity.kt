package fr.ceri.amiibo.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.ceri.amiibo.databinding.ActivityRuleBinding
import android.text.Html

class RuleActivity : AppCompatActivity() {

    private lateinit var ui: ActivityRuleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityRuleBinding.inflate(layoutInflater)
        setContentView(ui.root)

        //* Styles des textes
        ui.tvTitle.text = android.text.Html.fromHtml(
            "<font color='#C8007D'>Les règles </font><font color='#000000'><b>du quizz</b></font>", Html.FROM_HTML_MODE_LEGACY)
        ui.swipeRight.text = android.text.Html.fromHtml(
            "<font color='#000000'>Swipe à </font><font color='#C8007D'>Droite</font>", Html.FROM_HTML_MODE_LEGACY
        )
        ui.swipeLeft.text = Html.fromHtml(
            "<font color='#000000'>Swipe à </font><font color='#00AAEB'>Gauche</font>" , Html.FROM_HTML_MODE_LEGACY
        )

        setupListeners()
    }

    private fun setupListeners() {
        ui.btnUnderstood.setOnClickListener {
            finish()
        }
        ui.footerIconLeft.setOnClickListener {
            finish()
        }
    }
}
