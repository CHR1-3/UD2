import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class Videojuego(
    @SerialName("id") val id: Int,
    @SerialName("titulo") val titulo: String,
    @SerialName("genero") val genero: String,
    @SerialName("anio") val anio: Int,
    @SerialName("nota") val nota: Double
)