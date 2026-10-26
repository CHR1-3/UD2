# Catálogo de Videojuegos

Programa de consola en Kotlin para gestionar un catálogo de videojuegos.
Permite gestionar información mediante ficheros CSV, leer catálogos desde XML y JSON, realizar conversiones entre formatos y administrar los registros en un fichero binario de acceso aleatorio.
Cada videojuego le he puesto un ID, título, género, año de lanzamiento y una nota.

## 1. Estructura de datos

### **Data Class:**

```kotlin
@Serializable
data class Videojuego(
    @SerialName("id") val id: Int,
    @SerialName("titulo") val titulo: String,
    @SerialName("genero") val genero: String,
    @SerialName("anio") val anio: Int,
    @SerialName("nota") val nota: Double
)
```
### **Estructura del registro binario:**

- **ID**: Int - 4 bytes
- **titulo**: String - 100 bytes (longitud fija)
- **genero**: String - 40 bytes (longitud fija)
- **anio**: Int - 4 bytes
- **nota**: Double - 8 bytes
- **Tamaño Total del Registro**: **156 bytes**

Las cadenas de texto tienen una longitud fija y se rellenan con espacios cuando no ocupan todos los bytes reservados. 
Se utiliza ISO-8859-1 para almacenar las cadenas.

## 2. Instrucciones de ejecución

### Pasos para ejecutar la aplicación

1. Abrir el proyecto en IntelliJ IDEA.
2. Esperar a que Gradle sincronice las dependencias.
3. Comprobar que el fichero `videojuegos.csv` está en `datos/datos_ini/`.
4. Ejecutar `Main.kt`.
5. Seleccionar la opción deseada en el menú principal

Para utilizar la gestión binaria, seleccionar la opción **5. Gestión fichero BIN**.
La primera vez, seleccionar la opción de importar para crear el fichero binario con los datos del CSV. 
Después se podrán leer, añadir, modificar y eliminar videojuegos.

## 5. Decisiones de diseño

- Se utiliza CSV como fuente inicial porque es un formato sencillo de leer y modificar.
- Se mantiene una única clase `Videojuego` para representar los datos en los distintos formatos.
- Se utiliza un tamaño fijo de 156 bytes por registro binario para facilitar el acceso directo a cada videojuego.
- Se reservan 100 bytes para el título y 40 bytes para el género, dejando espacio para textos de distintos tamaños (mejor que sobre a que falte).
- Se utiliza `FileChannel` junto con `ByteBuffer` para leer y escribir los registros binarios.
- Para modificar un videojuego se localiza su posición en el fichero y se sobrescribe el registro.
- Para eliminar un videojuego se crea un fichero temporal con los registros que deben conservarse y después se sustituye el fichero original.
- Se comprueban los tipos de los datos introducidos y se evita añadir ID duplicados.

En general todo el proyecto está basado en la guía, documentación y/o apuntes proporcionados por el profesor José Manuel Abad López (Gran profesor por cierto)
Se ha intentado seguir los pasos al pie de la letra dentro de mis capacidades y entendimientos del proyecto.