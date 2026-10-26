import java.nio.file.Files
import java.nio.file.Path

fun main() {
    println("CREACIÓN DE RUTAS PROYECTO")
    println(" Creando rutas...")
    println(" Creando ficheros...")
    directorios()

    println("MOSTRANDO ESTRUCTURA DE DIRECTORIOS Y FICHEROS")
    mostrarEstructura()
    var lista = VideojuegoRepository.cargar()
    while (true) {
        println("""
===== CATÁLOGO DE VIDEOJUEGOS =====
1. Gestión CSV
2. Leer catálogo desde XML
3. Leer catálogo desde JSON
4. Conversiones entre formatos
5. Gestion ficheros BIN
0. Salir
====================================
Elige una opción:
        """.trimIndent())

        when (readlnOrNull()) {
            "1" -> menuCSV(lista)
            "2" -> importarXML(lista)
            "3" -> importarJSON(lista)
            "4" -> menuConversiones()
            "5" -> menuBIN()
            "0" -> return
            else -> println("Opción inválida")
        }
    }

}

fun directorios(){
    val datos: Path = Path.of("datos")
    val datos_ini: Path = Path.of("$datos/datos_ini")
    val datos_final: Path = Path.of("$datos/datos_final")
    val ruta: Path = Path.of("$datos_ini/videojuegos.csv")
    val xml: Path = Path.of("$datos_ini/videojuegos.xml")
    val json: Path = Path.of("$datos_ini/videojuegos.json")

    //Si no existen las carpetas se crean
    if (Files.notExists(datos_ini)) {
        Files.createDirectories(datos_ini)
    }
    if (Files.notExists(datos_final)) {
        Files.createDirectories(datos_final)
    }
    //Crear fichero si no existe
    if (Files.notExists(ruta)){
        Files.createFile(ruta)
    }
    if (Files.notExists(json)){
        Files.createFile(json)
    }
    if (Files.notExists(xml)){
        Files.createFile(xml)
    }

}

fun mostrarEstructura() {
    val datos = Path.of("datos")

    Files.list(datos).use { stream ->
        println(" [DIR] datos")
        stream.forEach { elemento ->
            if (Files.isDirectory(elemento)) {
                println("  [DIR] ${elemento.fileName}")

                // Listar contenido de datos_ini
                if (elemento.fileName.toString() == "datos_ini") {
                    Files.list(elemento).use { sub ->
                        sub.forEach { fichero ->
                            if (Files.isRegularFile(fichero)) {
                                println("   [FILE] ${fichero.fileName}")
                            }
                        }
                    }
                }
            }
        }
    }
}

fun menuCSV(lista: MutableList<Videojuego>) {
    while (true) {
        println("""
--- Gestión CSV ---
1. Listar todos los videojuegos
2. Buscar videojuego por ID
3. Añadir nuevo videojuego
4. Modificar nota de un videojuego
5. Eliminar videojuego
0. Volver al menú principal
        """.trimIndent())

        when (readlnOrNull()) {
            "1" -> VideojuegoService.listar(lista)

            "2" -> {
                println("ID:")
                val id = readlnOrNull()?.toIntOrNull()
                if (id != null) {
                    println(VideojuegoService.buscarPorId(lista, id) ?: "No encontrado")
                }
            }

            "3" -> {
                VideojuegoService.añadir(lista)
                VideojuegoRepository.guardar(lista)
            }

            "4" -> {
                println("ID:")
                val id = readlnOrNull()?.toIntOrNull()
                if (id != null) {
                    VideojuegoService.modificarNota(lista, id)
                    VideojuegoRepository.guardar(lista)
                }
            }

            "5" -> {
                println("ID:")
                val id = readlnOrNull()?.toIntOrNull()
                if (id != null) {
                    VideojuegoService.eliminar(lista, id)
                    VideojuegoRepository.guardar(lista)
                }
            }

            "0" -> return

            else -> println("Opción inválida")
        }
    }
}

fun importarXML(lista: MutableList<Videojuego>) {
    val rutaXML = Path.of("datos", "datos_ini", "videojuegos.xml")

    try {
        val videojuegosXML = leerVideojuegoXML(rutaXML)

        println("--- Videojuegos leídos desde XML ---")
        for (videojuego in videojuegosXML) {
            println(videojuego)
        }

        integrarCatalogo(lista, videojuegosXML)

    } catch (e: Exception) {
        println("Error al importar XML: ${e.message}")
    }
}


fun integrarCatalogo(
    lista: MutableList<Videojuego>,
    nuevosVideojuegos: List<Videojuego>
) {
    val idsExistentes = lista.map { it.id }.toMutableSet()
    var añadidos = 0

    for (videojuego in nuevosVideojuegos) {
        if (idsExistentes.add(videojuego.id)) {
            lista.add(videojuego)
            añadidos++
        }
    }

    VideojuegoRepository.guardar(lista)

    println("Importación completada.")
    println("Videojuegos leídos: ${nuevosVideojuegos.size}")
    println("Videojuegos nuevos añadidos: $añadidos")
    println("Total de videojuegos en el catálogo: ${lista.size}")
}

fun importarJSON(lista: MutableList<Videojuego>) {
    val rutaJSON = Path.of("datos", "datos_ini", "videojuegos.json")

    try {
        val videojuegosJSON = leerVideojuegosJSON(rutaJSON)

        println("--- Videojuegos leídos desde JSON ---")
        for (videojuego in videojuegosJSON) {
            println(videojuego)
        }

        integrarCatalogo(lista, videojuegosJSON)

    } catch (e: Exception) {
        println("Error al importar JSON: ${e.message}")
    }
}

fun convertir(
    origen: Path,
    destino: Path,
    leer: (Path) -> List<Videojuego>,
    escribir: (Path, List<Videojuego>) -> Unit
) {
    try {
        val videojuegos = leer(origen)
        escribir(destino, videojuegos)

        println("Conversión completada.")
        println("Fichero generado: $destino")
        println("Registros convertidos: ${videojuegos.size}")
    } catch (e: Exception) {
        println("Error durante la conversión: ${e.message}")
    }
}

fun menuConversiones() {
    while (true) {
        println(
            """
            --- CONVERSIONES ENTRE FORMATOS ---
            1. CSV -> JSON
            2. CSV -> XML
            3. JSON -> CSV
            4. JSON -> XML
            5. XML -> CSV
            6. XML -> JSON
            0. Volver
            Elige una opción:
            """.trimIndent()
        )

        when (readlnOrNull()) {
            "1" -> convertir(
                Path.of("datos", "datos_ini", "videojuegos.csv"),
                Path.of("datos", "datos_final", "videojuegos.json"),
                { VideojuegoRepository.cargar() },
                ::escribirVideojuegosJSON
            )

            "2" -> convertir(
                Path.of("datos", "datos_ini", "videojuegos.csv"),
                Path.of("datos", "datos_final", "videojuegos.xml"),
                { VideojuegoRepository.cargar() },
                ::escribirVideojuegoXML
            )

            "3" -> convertir(
                Path.of("datos", "datos_ini", "videojuegos.json"),
                Path.of("datos", "datos_final", "videojuegos.csv"),
                ::leerVideojuegosJSON,
                { ruta, lista -> VideojuegoRepository.guardarEnRuta(ruta, lista) }
            )

            "4" -> convertir(
                Path.of("datos", "datos_ini", "videojuegos.json"),
                Path.of("datos", "datos_final", "videojuegos.xml"),
                ::leerVideojuegosJSON,
                ::escribirVideojuegoXML
            )

            "5" -> convertir(
                Path.of("datos", "datos_ini", "videojuegos.xml"),
                Path.of("datos", "datos_final", "videojuegos.csv"),
                ::leerVideojuegoXML,
                { ruta, lista -> VideojuegoRepository.guardarEnRuta(ruta, lista) }
            )

            "6" -> convertir(
                Path.of("datos", "datos_ini", "videojuegos.xml"),
                Path.of("datos", "datos_final", "videojuegos.json"),
                ::leerVideojuegoXML,
                ::escribirVideojuegosJSON
            )

            "0" -> return
            else -> println("Opción inválida.")
        }
    }
}





