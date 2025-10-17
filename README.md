Lab 8 - Galería de Fotos con Room
Descripción
Este laboratorio se hizo en Kotlin con Jetpack Compose. La idea fue crear una app que se conecta a la API de Pexels para buscar fotos, mostrarlas en una cuadrícula y poder navegar entre pantallas. Además, se integró Room para tener persistencia local de fotos, favoritos y búsquedas recientes.
La app tiene tres partes principales:

Pantalla principal (Inicio): donde se hace la búsqueda y se muestran las fotos.
Pantalla de detalles (Details): donde se abre la foto más grande y se ven algunos datos.
Pantalla de perfil (Profile): donde se pone un avatar de ejemplo, nombre y correo falso, y un switch para cambiar entre tema claro y oscuro.

Lo que se implementó
Funcionalidad básica

Una barra de búsqueda que funciona con debounce de 500 ms, o sea que no manda una consulta por cada letra que uno escribe, sino que espera medio segundo para hacer la búsqueda real.
Los resultados aparecen en una cuadrícula de fotos que carga más imágenes cuando uno baja, o sea con scroll infinito (paginación automática).
Si tocas una foto, se abre la pantalla de detalles con la imagen en grande, el autor y algunos detalles o información básica.
En la pantalla de perfil hay un avatar fijo, nombre y correo de ejemplo, y un switch que deja cambiar entre tema claro y oscuro.
Todo está hecho con Material 3 para que cambie bien entre light y dark.

Persistencia con Room (nuevo en Lab 8)

Cache por consulta: cuando buscas algo, los resultados se guardan en la base de datos local asociados a la búsqueda y la página. Si volvés a buscar lo mismo, carga instantáneo desde cache.
Favoritos persistentes: podés marcar fotos como favoritas con el corazón y ese estado se guarda en Room. Cuando cerrás y volvés a abrir la app, los favoritos siguen ahí.
Búsquedas recientes: la app guarda las últimas 10 búsquedas que hiciste y te las muestra como chips para que las reutilices fácilmente.
Modo offline básico: si perdés la conexión, la app te muestra las fotos que tenés en cache de búsquedas anteriores. Aparece un indicador "Modo Offline" cuando esto pasa.

Manejo de estado
En este laboratorio no usamos ViewModel, sino que todo se resolvió con remember y rememberSaveable.

La query de la búsqueda se guarda con rememberSaveable, para que no se pierda aunque rote la pantalla.
El tema (claro/oscuro) también está guardado en un booleano con rememberSaveable.
Para el debounce usamos snapshotFlow { query } con debounce(500), dentro de un LaunchedEffect.
Los favoritos y cache se manejan con Room directamente desde los Composables.

Organización del proyecto

pantallas/ → tres pantallas: Inicio, Detalles y Perfil.
navegacion/ → Rutas y navegación entre pantallas.
internet/ → Código para conectarse a la API de Pexels.
database/ → Entidades de Room (PhotoEntity, RecentSearchEntity), DAOs y la base de datos.
repository/ → Lógica de cache-first: intenta red primero, si falla usa cache.
componentes/ → componentes que se repiten, como la tarjeta de foto.
ui.theme/ → Colores y temas claro/oscuro.
datos/ → modelos y datos de prueba.

Modelo de datos (Room)
PhotoEntity
Almacena las fotos con estos campos:

id, photographer, urlMedium, urlLarge, width, height
queryKey: la búsqueda normalizada a la que pertenece
pageIndex: número de página
isFavorite: si está marcada como favorita
updatedAt: timestamp para saber cuándo se guardó

Tiene índices en (queryKey, pageIndex) y en isFavorite para búsquedas eficientes.
RecentSearchEntity
Guarda las búsquedas recientes:

query: texto de la búsqueda (normalizado y único)
lastUsedAt: timestamp del último uso

Se mantienen solo las últimas 10 búsquedas.
Estrategia de cache y paginación
Cuando hacés una búsqueda:

Se normaliza el query (trim + lowercase)
Se intenta cargar desde la API de Pexels
Si hay éxito → se guardan los resultados en Room con (queryKey, pageIndex)
Si falla la red → se intenta cargar desde Room (modo offline)
La búsqueda se guarda en recent_searches

La paginación es manual (no usamos RemoteMediator de Paging 3):

Detectamos cuando estás cerca del final de la lista
Cargamos la página siguiente automáticamente
Cada página se persiste por separado en Room

