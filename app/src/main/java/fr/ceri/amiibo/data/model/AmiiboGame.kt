package fr.ceri.amiibo.data.model

// Classe de données représentant un objet AmiiboGame.
// Elle contient deux propriétés : une clé unique et un nom.

data class AmiiboGame(
    val key: String,      // Clé unique identifiant l'amiibo (par exemple : "0x01").
    val name: String      // Nom de l'amiibo (par exemple : "Mario").
)
