import java.nio.file.Files
import java.nio.file.Path

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

@JacksonXmlRootElement(localName = "videojuegos")
data class VideojuegosXML(
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "videojuego")
    val videojuegos: List<Videojuego> = emptyList()
)

fun leerVideojuegoXML(ruta: Path): List<Videojuego> {
    require(Files.isRegularFile(ruta) && Files.isReadable(ruta)) {
        "No se encuentra el fichero XML o no se puede leer: $ruta"
    }

    val xmlMapper = XmlMapper().registerKotlinModule()
    val contenedor: VideojuegosXML = xmlMapper.readValue(ruta.toFile())

    println("XML leído correctamente: $ruta")
    return contenedor.videojuegos
}

fun escribirVideojuegoXML(ruta: Path, lista: List<Videojuego>) {
    ruta.parent?.let { Files.createDirectories(it) }

    val xmlMapper = XmlMapper().registerKotlinModule()

    xmlMapper.writerWithDefaultPrettyPrinter()
        .writeValue(ruta.toFile(), VideojuegosXML(lista))

    println("XML guardado correctamente: $ruta")
}


