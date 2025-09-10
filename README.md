# Lab06

# Descripción
Este laboratorio se hizo en Kotlin con Jetpack Compose.  
La idea fue crear una app que se conecta a la API de Pexels para buscar fotos, mostrarlas en una cuadrícula de fotos y poder navegar entre pantallas.  

La app tiene tres partes principales:
- Pantalla principal (Inicio): donde se hace la búsqueda y se muestran las fotos.
- Pantalla de detalles (Details): donde se abre la foto más grande y se ven algunos datos.
- Pantalla de perfil (Profile): donde se pone un avatar de ejemplo, nombre y correo falso, y un switch para cambiar entre tema claro y oscuro.


# Lo que se implementó
- Una barra de búsqueda que funciona con debounce. o sea que no manda una consulta por cada letra que uno escribe, sino que espera medio segundo (500 ms) para hacer la búsqueda real.  
- Los resultados aparecen en una cuadrícula de fotos que carga más imágenes cuando uno baja, o sea con scroll infinito.  
- Si tocas una foto, se abre la pantalla de detalles con la imagen en grande, el autor y algunos detalles o información básica.  
- En la pantalla de perfil hay un avatar fijo, nombre y correo de ejemplo, y un switch que deja cambiar entre tema claro y oscuro.  
- Todo está hecho con Material 3 para que cambie bien entre light y dark.  


# Manejo de estado
En este laboratorio no usamos ViewModel, sino que todo se resolvió con `remember` y `rememberSaveable`.  
- La query de la búsqueda se guarda con `rememberSaveable`, para que no se pierda aunque rote la pantalla.  
- El tema (claro/oscuro) también está guardado en un booleano con `rememberSaveable`.  
- Para el debounce usamos `snapshotFlow { query }` con `debounce(500)`, dentro de un `LaunchedEffect`.  


# Organización del proyecto
- pantallas/ → tres pantallas: Inicio, Detalles y Perfil.  
- navegacion/ → Rutas y navegación entre pantallas.  
- internet/ → Código para conectarse a la API de Pexels.  
- componentes/ → componentes que se repiten, como la tarjeta de foto.  
- ui.theme/ → Colores y temas claro/oscuro.
- datos/ → modelos y datos de prueba.  


# Demo en video
link del video: https://youtu.be/u4IDXCtY3JY







