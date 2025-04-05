package fr.ceri.amiibo.ui

import android.R
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import fr.ceri.amiibo.databinding.ActivityLanguageBinding

class LanguageActivity : AppCompatActivity() {

    private lateinit var ui: ActivityLanguageBinding
    private val languages = listOf(
        "Français", "Sénégalais", "Africain", "Congolais",
        "Catégorie 1", "Catégorie 2", "Catégorie 3", "Catégorie 4", "Catégorie 5"
    )
    private var selectedLanguage: String? = null
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(ui.root)

        setupList()
        setupListeners()
    }

    private fun setupList() {
        adapter = object : ArrayAdapter<String>(
            this,
            R.layout.simple_list_item_single_choice,
            languages
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val isSelected = languages[position] == selectedLanguage
                view.setBackgroundColor(if (isSelected) getColor(fr.ceri.amiibo.R.color.red) else getColor(
                    R.color.transparent))
                val textView = view.findViewById<TextView>(R.id.text1)
                textView.setTextColor(if (isSelected) getColor(R.color.white) else getColor(R.color.black))
                return view
            }
        }

        ui.listViewLanguages.adapter = adapter
        ui.listViewLanguages.choiceMode = ListView.CHOICE_MODE_SINGLE

        ui.listViewLanguages.setOnItemClickListener { _, _, position, _ ->
            selectedLanguage = languages[position]
            adapter.notifyDataSetChanged()
        }
    }

    private fun setupListeners() {
        ui.btnConfirm.setOnClickListener {
            if (selectedLanguage == null) {
                Toast.makeText(this, "Choisis une langue", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Langue sélectionnée : $selectedLanguage", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // Retour via icône
        ui.footerIconLeft.setOnClickListener {
            finish()
        }
    }
}
