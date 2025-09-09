package com.tuusuario.lab6.datos

// función para crear fotos de prueba y probar el scroll
fun generarFotosDePrueba(total: Int = 200): List<Foto> {
    return (1..total).map { i ->
        Foto(
            id = i.toString(),
            nombreAutor = "Autor $i",
            urlPequena = "https://picsum.photos/seed/$i/300/300",
            urlGrande = "https://picsum.photos/seed/$i/800/600"
        )
    }
}
