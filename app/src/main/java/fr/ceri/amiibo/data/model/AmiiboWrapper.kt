package fr.ceri.amiibo.data.model

// Classe servant de wrapper (ou enveloppe) pour parser la réponse de l’API Amiibo.
// L’API renvoie un objet JSON contenant une clé "amiibo" qui englobe une liste d’objets Amiibo.

data class AmiiboWrapper(
    val amiibo: List<Amiibo> // Liste des amiibos retournés par l’API.
)
