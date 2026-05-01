# MVP-1: chat Markdown exportable

## Definición

Una CLI permite crear usuarios y salas, enviar mensajes Markdown válidos,
consultar la conversación ordenada y exportar una sala como documento Markdown.
Cada usuario recibe una sala personal privada al crearse.

## Promesa demostrable

Un usuario puede crear o entrar a una sala, escribir mensajes Markdown válidos,
ver la conversación ordenada y exportarla como Markdown.

Esto prueba la tesis central del producto sin construir todavía WebSocket, UI web,
bots activos ni permisos complejos:

```text
conversación Markdown
  -> historial ordenado
  -> documento Markdown exportable
```

## Alcance funcional

### Usuarios mínimos

- Crear usuario con `handle`.
- Distinguir tipo de usuario en el modelo.
- Al crear un usuario humano, crear automáticamente una sala personal privada.

### Salas mínimas

- Crear sala compartida.
- Listar salas.
- Consultar una sala.
- Soportar salas personales y compartidas.

### Mensajes Markdown

- Enviar mensaje Markdown a una sala.
- Validar Markdown mínimo.
- Rechazar HTML crudo y contenido inseguro.
- Guardar `body-markdown` como fuente de verdad.
- Crear evento `message/created`.
- Mantener idempotencia por `author-id + client-txn-id`.

### Conversación

- Ver mensajes de una sala ordenados por secuencia.
- Mantener historial básico.

### Exportación Markdown

- Exportar una sala como documento Markdown.
- Incluir título de sala.
- Incluir mensajes con autor y contenido Markdown original.

## Interfaz inicial

La interfaz inicial recomendada es CLI para evitar construir UI web y WebSocket
antes de validar el núcleo.

Ejemplo deseado:

```sh
chat create-user andres
chat create-room general
chat send general andres "Hola **mundo**" client-txn-1
chat show general
chat export general
```

## Fuera de alcance

- WebSocket realtime.
- UI web.
- Bots o agentes activos.
- Mensajes directos.
- Moderación.
- Login real.
- Render HTML completo.
- Linting avanzado.
- Branches o forks.
- Búsqueda.
- Adjuntos.
- Notificaciones.
- Permisos finos.

## User stories

- Como usuario, quiero crearme con un handle para poder participar.
- Como usuario, quiero tener una sala personal automática para capturar ideas.
- Como usuario, quiero crear una sala compartida para conversar con otros.
- Como usuario, quiero enviar mensajes Markdown válidos a una sala.
- Como usuario, quiero ver la conversación ordenada.
- Como usuario, quiero exportar una sala como Markdown para conservar
  conocimiento.
