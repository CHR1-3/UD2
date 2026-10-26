import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.nio.file.Files
import java.nio.file.Path

object VideojuegoRepository {

    private val rutaCSV = Path.of("datos", "datos_ini", "videojuegos.csv")

    fun cargar(): MutableList<Videojuego> {
        if (!Files.exists(rutaCSV)) {
            println("VideojuegoRepository cargar not found")
            return mutableListOf()
        }

        val reader = csvReader { delimiter = ';' }
        val filas = reader.readAll(rutaCSV.toFile())

        val lista = filas.mapNotNull { columnas ->
            if (columnas.size < 5) return@mapNotNull null

            try {
                Videojuego(
                    columnas[0].toInt(),
                    columnas[1],
                    columnas[2],
                    columnas[3].toInt(),
                    columnas[4].toDouble()
                )
            } catch (e: Exception) {
                println("Fila ignorada: $columnas")
                null
            }
        }

        return lista.toMutableList()
    }

    fun guardar(lista: List<Videojuego>) {
        val writer = csvWriter { delimiter = ';' }

        writer.writeAll(
            lista.map{
                listOf(
                    it.id.toString(),
                    it.titulo,
                    it.genero,
                    it.anio.toString(),
                    it.nota.toString()
                )
            },
            rutaCSV.toFile()
        )
    }

    fun guardarEnRuta(ruta: Path, lista: List<Videojuego>) {
        ruta.parent?.let { Files.createDirectories(it) }

        val writer = csvWriter {
            delimiter = ';'
        }

        writer.writeAll(
            lista.map {
                listOf(
                    it.id.toString(),
                    it.titulo,
                    it.genero,
                    it.anio.toString(),
                    it.nota.toString()
                )
            },
            ruta.toFile()
        )

        println("CSV guardado correctamente: $ruta")
    }

}