package fr.ceri.amiibo.data.model

//* Classe Amiibo qui représente un amiibo
data class Amiibo(
    val name: String,       // ex: "Mario"
    val image: String,      // ex: "https://amiiboapi.com/images/amiibo/mario.png"
    val gameSeries: String  // ex: "Super Mario Bros."
)
