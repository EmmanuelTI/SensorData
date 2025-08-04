package equipo.dinamita.otys.dbsqlite.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val birthdate: String = "",
    val createdAt: Long = 0L
)