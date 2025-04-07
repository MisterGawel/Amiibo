package fr.ceri.amiibo.data.model


//* Le wrapper sert à parser la réponse de l'API
data class AmiiboWrapper(
    val amiibo: List<Amiibo>
)
