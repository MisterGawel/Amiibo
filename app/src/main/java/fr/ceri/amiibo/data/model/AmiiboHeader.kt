package fr.ceri.amiibo.data.model

// Classe de données représentant un en-tête d’amiibo,
// contenant une liste mutable d’objets AmiiboGame.

data class AmiiboHeader(
    val amiibo: MutableList<AmiiboGame> // Liste modifiable d’objets AmiiboGame.
)
