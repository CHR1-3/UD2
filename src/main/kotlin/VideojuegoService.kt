object VideojuegoService {

    fun listar(lista: List<Videojuego>) {
        println("\n--- LISTADO DE VIDEOJUEGOS ---")
        lista.forEach {
            println("${it.id}. ${it.titulo} | ${it.genero} | ${it.anio} | Nota: ${it.nota}")
        }
    }

    fun buscarPorId(lista: List<Videojuego>, id: Int): Videojuego? {
        return lista.find { it.id == id }
    }

    fun añadir(lista: MutableList<Videojuego>) {
        println("Título:")
        val titulo = readlnOrNull()?.trim().orEmpty()

        println("Género:")
        val genero = readlnOrNull()?.trim().orEmpty()

        println("Año:")
        val anio = readlnOrNull()?.toIntOrNull() ?: return println("Año inválido")

        println("Nota (0.0 - 10.0):")
        val nota = readlnOrNull()?.toDoubleOrNull() ?: return println("Nota inválida")

        val nuevoId = (lista.maxOfOrNull { it.id } ?: 0) + 1

        lista.add(Videojuego(nuevoId, titulo, genero, anio, nota))
        println("Videojuego añadido con ID $nuevoId")
    }

    fun modificarNota(lista: MutableList<Videojuego>, id: Int) {
        val juego = lista.find { it.id == id }
        if (juego == null) {
            println("No existe un videojuego con ese ID")
            return
        }

        println("Nueva nota:")
        val nuevaNota = readlnOrNull()?.toDoubleOrNull()
        if (nuevaNota == null || nuevaNota !in 0.0..10.0) {
            println("Nota inválida")
            return
        }

        val actualizado = juego.copy(nota = nuevaNota)
        lista[lista.indexOf(juego)] = actualizado
        println("Nota actualizada correctamente")
    }

    fun eliminar(lista: MutableList<Videojuego>, id: Int) {
        val juego = lista.find { it.id == id }
        if (juego == null) {
            println("No existe un videojuego con ese ID")
            return
        }

        println("¿Seguro que quieres eliminarlo? (s/n)")
        val confirm = readlnOrNull()?.lowercase()

        if (confirm == "s") {
            lista.remove(juego)
            println("Videojuego eliminado")
        } else {
            println("Cancelado")
        }
    }
}
