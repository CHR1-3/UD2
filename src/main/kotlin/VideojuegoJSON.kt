import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val formatoJSON = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}

fun leerVideojuegosJSON(ruta: Path): List<Videojuego> {
    require(Files.isRegularFile(ruta) && Files.isReadable(ruta)) {
        "No se encuentra el fichero JSON o no se puede leer: $ruta"
    }

    val contenido = Files.readString(ruta)
    val videojuegos: List<Videojuego> =
        formatoJSON.decodeFromString(contenido)

    println("JSON leído correctamente: $ruta")
    return videojuegos
}

fun escribirVideojuegosJSON(
    ruta: Path,
    lista: List<Videojuego>
) {
    ruta.parent?.let { Files.createDirectories(it) }

    val contenido = formatoJSON.encodeToString(lista)
    Files.writeString(ruta, contenido)

    println("JSON guardado correctamente: $ruta")
}

