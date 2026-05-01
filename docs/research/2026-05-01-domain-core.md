# Núcleo de dominio inicial

> Nota: este documento registra una exploración previa al reinicio TDD. El estado
> actual del código avanza nuevamente en slices más pequeños.

## Estado después del reinicio TDD

El primer slice real implementado es la historia #20: crear un usuario humano con
handle.

La API actual es deliberadamente mínima:

```clojure
(pi-clojure.domain.user/create-human "andres")
;; => #:user{:handle "andres"
;;           :type :user.type/human}
```

Decisiones actuales:

- Un usuario humano se representa como mapa con `:user/handle` y `:user/type`.
- El tipo de usuario humano es `:user.type/human`.
- El test de dominio diseña la API desde el consumidor.

Fuera de alcance por ahora:

- id de usuario;
- persistencia;
- validación de handle;
- sala personal automática;
- CLI.

## Decisión exploratoria previa

El primer núcleo programado es el caso de uso `send-message` con almacenamiento
en memoria y tests.

El objetivo es probar el centro del producto antes de integrar WebSocket, UI o
Dolt desde Clojure:

```text
usuario
  -> envía mensaje Markdown a sala
  -> validación mínima
  -> persistencia en memoria
  -> evento append-only
  -> conversación reconstruible por secuencia
```

## Alcance implementado

- Modelo mínimo de dominio para usuarios humanos, usuarios agentes, salas
  personales y salas compartidas.
- Store en memoria para usuarios, salas, mensajes, eventos, secuencias y
  transacciones de cliente.
- `create-user` crea usuarios humanos y su sala personal privada.
- `create-user` rechaza handles vacíos o duplicados.
- `create-room` crea salas compartidas.
- `create-room` rechaza títulos vacíos o duplicados.
- `list-rooms` lista salas disponibles.
- `send-message` crea un mensaje y un evento `:message/created`.
- Validación mínima de Markdown:
  - rechaza mensajes no textuales;
  - rechaza mensajes vacíos;
  - rechaza mensajes demasiado largos;
  - rechaza HTML crudo;
  - rechaza links con protocolos `javascript:` o `data:`.
- Idempotencia por `author-id + client-txn-id`.
- Consulta de conversación ordenada por secuencia dentro de la sala.
- Exportación básica de sala como documento Markdown.

## Decisiones de diseño

- Humanos y agentes comparten las mismas capacidades base; el tipo de usuario no
  otorga permisos especiales implícitos.
- Las salas personales son privadas y tienen propietario desde el modelo.
- `body-markdown` sigue siendo la fuente de verdad.
- El HTML renderizado queda fuera de este primer slice.
- La validación Markdown es deliberadamente mínima y deberá reemplazarse o
  complementarse con un parser real.
- El store en memoria permite diseñar el dominio con TDD sin acoplarse todavía a
  Dolt o JDBC.

## Comando de verificación

```sh
clojure -M:test
```
