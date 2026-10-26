
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

object VideojuegoBinRepository {

    private val ruta = Path.of(
        "datos", "datos_final", "videojuegos.bin"
    )

    private const val TAM_TITULO = 100
    private const val TAM_GENERO = 40

    private const val TAM_REGISTRO =
        4 + TAM_TITULO + TAM_GENERO + 4 + 8

    private val charset = StandardCharsets.ISO_8859_1

    private fun escribirCadena(
        buffer: ByteBuffer,
        texto: String,
        longitud: Int
    ) {
        val bytes = texto.toByteArray(charset)

        require(bytes.size <= longitud) {
            "El texto supera el máximo de $longitud bytes."
        }

        buffer.put(bytes)

        repeat(longitud - bytes.size) {
            buffer.put(' '.code.toByte())
        }
    }

    private fun leerCadena(
        buffer: ByteBuffer,
        longitud: Int
    ): String {
        val bytes = ByteArray(longitud)
        buffer.get(bytes)

        return bytes.toString(charset).trimEnd()
    }

    private fun crearBuffer(
        videojuego: Videojuego
    ): ByteBuffer {
        val buffer = ByteBuffer.allocate(TAM_REGISTRO)
            .order(ByteOrder.BIG_ENDIAN)

        buffer.putInt(videojuego.id)
        escribirCadena(buffer, videojuego.titulo, TAM_TITULO)
        escribirCadena(buffer, videojuego.genero, TAM_GENERO)
        buffer.putInt(videojuego.anio)
        buffer.putDouble(videojuego.nota)

        buffer.flip()
        return buffer
    }

    private fun leerRegistro(
        buffer: ByteBuffer
    ): Videojuego {
        val id = buffer.int
        val titulo = leerCadena(buffer, TAM_TITULO)
        val genero = leerCadena(buffer, TAM_GENERO)
        val anio = buffer.int
        val nota = buffer.double

        return Videojuego(id, titulo, genero, anio, nota)
    }

    private fun leerBufferCompleto(
        canal: FileChannel,
        buffer: ByteBuffer
    ): Boolean {
        while (buffer.hasRemaining()) {
            val leidos = canal.read(buffer)

            if (leidos == -1) {
                return false
            }

            if (leidos == 0) {
                Thread.yield()
            }
        }

        buffer.flip()
        return true
    }

    private fun escribirBufferCompleto(
        canal: FileChannel,
        buffer: ByteBuffer
    ) {
        while (buffer.hasRemaining()) {
            canal.write(buffer)
        }
    }

    fun leerTodos(): MutableList<Videojuego> {
        if (!Files.exists(ruta)) {
            return mutableListOf()
        }

        FileChannel.open(
            ruta,
            StandardOpenOption.READ
        ).use { canal ->

            require(canal.size() % TAM_REGISTRO == 0L) {
                "El fichero binario tiene un tamaño incorrecto."
            }

            val lista = mutableListOf<Videojuego>()
            val buffer = ByteBuffer.allocate(TAM_REGISTRO)
                .order(ByteOrder.BIG_ENDIAN)

            canal.position(0)

            while (canal.position() < canal.size()) {
                buffer.clear()

                check(leerBufferCompleto(canal, buffer)) {
                    "No se ha podido leer un registro completo."
                }

                lista.add(leerRegistro(buffer))
            }

            return lista
        }
    }

    fun importar(lista: List<Videojuego>) {
        require(lista.map { it.id }.distinct().size == lista.size) {
            "No se puede importar: hay IDs duplicados."
        }

        ruta.parent?.let {
            Files.createDirectories(it)
        }

        FileChannel.open(
            ruta,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING
        ).use { canal ->

            for (videojuego in lista) {
                escribirBufferCompleto(
                    canal,
                    crearBuffer(videojuego)
                )
            }
        }
    }

    fun anadir(videojuego: Videojuego) {
        require(leerTodos().none { it.id == videojuego.id }) {
            "Ya existe un videojuego con ID ${videojuego.id}."
        }

        ruta.parent?.let {
            Files.createDirectories(it)
        }

        FileChannel.open(
            ruta,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE
        ).use { canal ->
            canal.position(canal.size())

            escribirBufferCompleto(
                canal,
                crearBuffer(videojuego)
            )
        }
    }

    fun modificar(
        id: Int,
        actualizado: Videojuego
    ): Boolean {
        if (!Files.exists(ruta)) {
            return false
        }

        FileChannel.open(
            ruta,
            StandardOpenOption.READ,
            StandardOpenOption.WRITE
        ).use { canal ->

            require(canal.size() % TAM_REGISTRO == 0L) {
                "El fichero binario tiene un tamaño incorrecto."
            }

            var posicion = 0L

            val buffer = ByteBuffer.allocate(TAM_REGISTRO)
                .order(ByteOrder.BIG_ENDIAN)

            while (posicion < canal.size()) {
                buffer.clear()
                canal.position(posicion)

                check(leerBufferCompleto(canal, buffer)) {
                    "No se ha podido leer el registro."
                }

                val videojuego = leerRegistro(buffer)

                if (videojuego.id == id) {
                    require(actualizado.id == id) {
                        "El ID del registro no se puede cambiar."
                    }

                    canal.position(posicion)

                    escribirBufferCompleto(
                        canal,
                        crearBuffer(actualizado)
                    )

                    return true
                }

                posicion += TAM_REGISTRO
            }
        }

        return false
    }

    fun eliminar(id: Int): Boolean {
        val videojuegos = leerTodos()

        if (videojuegos.none { it.id == id }) {
            return false
        }

        val directorio = ruta.parent
            ?: Path.of(".")

        val temporal = Files.createTempFile(
            directorio,
            "videojuegos_",
            ".tmp"
        )

        try {
            FileChannel.open(
                temporal,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
            ).use { canal ->

                for (videojuego in videojuegos) {
                    if (videojuego.id != id) {
                        escribirBufferCompleto(
                            canal,
                            crearBuffer(videojuego)
                        )
                    }
                }
            }

            Files.move(
                temporal,
                ruta,
                StandardCopyOption.REPLACE_EXISTING
            )

            return true
        } finally {
            Files.deleteIfExists(temporal)
        }
    }
}

private fun pedirEntero(mensaje: String): Int {
    while (true) {
        print(mensaje)

        val valor = readlnOrNull()
            ?.trim()
            ?.toIntOrNull()

        if (valor != null) {
            return valor
        }

        println("Introduce un número entero válido.")
    }
}

private fun pedirDouble(mensaje: String): Double {
    while (true) {
        print(mensaje)

        val valor = readlnOrNull()
            ?.trim()
            ?.replace(',', '.')
            ?.toDoubleOrNull()

        if (valor != null && valor.isFinite()) {
            return valor
        }

        println("Introduce un número decimal válido.")
    }
}

private fun pedirTexto(
    mensaje: String,
    longitudMaxima: Int
): String {
    while (true) {
        print(mensaje)

        val texto = readlnOrNull()?.trim().orEmpty()
        val bytes = texto.toByteArray(StandardCharsets.ISO_8859_1)

        if (
            texto.isNotBlank() &&
            bytes.size <= longitudMaxima &&
            texto.all { it.code <= 255 }
        ) {
            return texto
        }

        println(
            "El texto no puede estar vacío y debe ocupar " +
                    "como máximo $longitudMaxima bytes."
        )
    }
}

fun menuBIN() {
    while (true) {
        println(
            """
            ===== GESTIÓN FICHERO BIN =====
            1. Importar información de un fichero de intercambio
            2. Leer información del fichero binario
            3. Añadir un registro nuevo
            4. Modificar un registro existente (por ID)
            5. Eliminar un registro existente (por ID)
            0. Volver
            ===============================
            """.trimIndent()
        )

        when (readlnOrNull()?.trim()) {
            "1" -> {
                try {
                    val videojuegos = VideojuegoRepository.cargar()

                    VideojuegoBinRepository.importar(videojuegos)

                    println(
                        "Importación completada: " +
                                "${videojuegos.size} registros."
                    )
                } catch (e: Exception) {
                    println("Error al importar: ${e.message}")
                }
            }

            "2" -> {
                try {
                    val videojuegos = VideojuegoBinRepository.leerTodos()

                    if (videojuegos.isEmpty()) {
                        println("El fichero binario está vacío.")
                    } else {
                        videojuegos.forEach {
                            println(it)
                        }

                        println("Total: ${videojuegos.size} registros.")
                    }
                } catch (e: Exception) {
                    println("Error al leer: ${e.message}")
                }
            }

            "3" -> {
                try {
                    var id: Int

                    do {
                        id = pedirEntero("ID: ")

                        if (
                            VideojuegoBinRepository
                                .leerTodos()
                                .any { it.id == id }
                        ) {
                            println("Ese ID ya existe. Prueba otro.")
                        }
                    } while (
                        VideojuegoBinRepository
                            .leerTodos()
                            .any { it.id == id }
                    )

                    val titulo = pedirTexto("Título: ", 100)
                    val genero = pedirTexto("Género: ", 40)
                    val anio = pedirEntero("Año: ")
                    val nota = pedirDouble("Nota: ")

                    VideojuegoBinRepository.anadir(
                        Videojuego(
                            id = id,
                            titulo = titulo,
                            genero = genero,
                            anio = anio,
                            nota = nota
                        )
                    )

                    println("Videojuego añadido correctamente.")
                } catch (e: Exception) {
                    println("Error al añadir: ${e.message}")
                }
            }

            "4" -> {
                try {
                    val id = pedirEntero(
                        "ID del videojuego a modificar: "
                    )

                    val existente = VideojuegoBinRepository
                        .leerTodos()
                        .firstOrNull { it.id == id }

                    if (existente == null) {
                        println(
                            "No existe ningún videojuego con ID $id."
                        )
                    } else {
                        println("Encontrado: ${existente.titulo}")

                        val titulo = pedirTexto("Nuevo título: ", 100)
                        val genero = pedirTexto("Nuevo género: ", 40)
                        val anio = pedirEntero("Nuevo año: ")
                        val nota = pedirDouble("Nueva nota: ")

                        val actualizado = existente.copy(
                            titulo = titulo,
                            genero = genero,
                            anio = anio,
                            nota = nota
                        )

                        if (
                            VideojuegoBinRepository.modificar(
                                id,
                                actualizado
                            )
                        ) {
                            println(
                                "Videojuego modificado correctamente."
                            )
                        } else {
                            println("No se ha encontrado el videojuego.")
                        }
                    }
                } catch (e: Exception) {
                    println("Error al modificar: ${e.message}")
                }
            }

            "5" -> {
                try {
                    val id = pedirEntero(
                        "ID del videojuego a eliminar: "
                    )

                    val existente = VideojuegoBinRepository
                        .leerTodos()
                        .firstOrNull { it.id == id }

                    if (existente == null) {
                        println(
                            "No existe ningún videojuego con ID $id."
                        )
                    } else {
                        println("Encontrado: ${existente.titulo}")

                        print(
                            "¿Seguro que quieres eliminarlo? (s/n): "
                        )

                        when (
                            readlnOrNull()
                                ?.trim()
                                ?.lowercase()
                        ) {
                            "s", "si", "sí" -> {
                                if (
                                    VideojuegoBinRepository.eliminar(id)
                                ) {
                                    println(
                                        "Videojuego eliminado correctamente."
                                    )
                                } else {
                                    println(
                                        "No se ha podido eliminar."
                                    )
                                }
                            }

                            else -> {
                                println("Eliminación cancelada.")
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("Error al eliminar: ${e.message}")
                }
            }

            "0" -> return

            else -> println("Opción inválida.")
        }
    }
}
