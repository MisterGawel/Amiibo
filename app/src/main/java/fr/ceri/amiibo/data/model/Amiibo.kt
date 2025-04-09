package fr.ceri.amiibo.data.model

// Classe de données représentant un amiibo.
// Elle contient les informations essentielles d’un amiibo affichées à l’utilisateur ou utilisées dans l’application.

data class Amiibo(
    val name: String,       // Nom de l'amiibo (exemple : "Mario").
    val image: String,      // URL de l'image de l'amiibo (exemple : "https://amiiboapi.com/images/amiibo/mario.png").
    val gameSeries: String  // Nom de la série de jeux à laquelle appartient l'amiibo (exemple : "Super Mario Bros.").
)
