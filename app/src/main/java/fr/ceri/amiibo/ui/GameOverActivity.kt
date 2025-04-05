package fr.ceri.amiibo.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import fr.ceri.amiibo.databinding.ActivityGameoverBinding
import fr.ceri.amiibo.shared.SharedSettings

class GameOverActivity : AppCompatActivity() {

    private lateinit var ui: ActivityGameoverBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityGameoverBinding.inflate(layoutInflater)
        setContentView(ui.root)

        val score = SharedSettings.lastScore
        ui.tvFinalScore.text = "$score"

        ui.btnReplay.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        ui.btnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}
