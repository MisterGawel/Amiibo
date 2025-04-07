package fr.ceri.amiibo.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.ceri.amiibo.databinding.ActivityRuleBinding
import android.text.Html
import fr.ceri.amiibo.R

class RuleActivity : AppCompatActivity() {

    private lateinit var ui: ActivityRuleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityRuleBinding.inflate(layoutInflater)
        setContentView(ui.root)

        //* Styles des textes
        styleTitle()
        styleSwipeTexts()

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

    private fun styleSwipeTexts() {
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

