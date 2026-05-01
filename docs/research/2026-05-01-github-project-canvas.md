# GitHub Project como canvas del MVP

## Contexto

Durante el refinamiento del backlog se decidió simplificar el GitHub Project para
que muestre el producto como un mapa de MVP, no solo como una lista de tareas.

La fuente operativa es el proyecto de GitHub:

- [`pi-clojure MVP`](https://github.com/users/andrestobelem/projects/2)

## Decisión

Usar tres campos principales en el board:

- `Status`: estado operativo de la historia.
- `Foco`: prioridad de ejecución.
- `Canvas`: ubicación conceptual dentro del MVP.

## Campo `Foco`

Opciones:

- `Ahora`: trabajo activo o inmediatamente siguiente; máximo 1-2 historias.
- `Siguiente`: camino mínimo para completar el MVP.
- `Después`: hardening, experiencia o mejoras no necesarias para el primer
  recorrido punta a punta.
- `Pausado`: trabajo útil pero no necesario para el corte actual.

## Campo `Canvas`

Opciones:

- `Hecho`: base ya validada.
- `Fundación`: modelo mínimo que sostiene el resto.
- `Salas`: creación y descubrimiento de salas.
- `Participación`: entrar, leer, escribir y salir.
- `Mensajes`: envío, orden, validación, seguridad e idempotencia.
- `Documento`: exportación Markdown.
- `Producto`: experiencia de uso, inicialmente CLI.

Vista conceptual:

```text
Hecho
  -> Fundación
  -> Salas
  -> Participación
  -> Mensajes
  -> Documento
  -> Producto
```

## Regla de foco

No empezar historias de `Después` hasta poder recorrer el flujo de `Siguiente` de
punta a punta en memoria.

Mantener como máximo 1-2 historias en `In Progress`.

## Flujo objetivo del MVP

```sh
chat create-user andres
chat create-room general
chat join general andres
chat send general andres "Hola **mundo**"
chat show general andres
chat export general andres
chat leave general andres
```

## Limpieza de backlog

Cuando una tarea técnica queda absorbida por una historia de usuario, conviene
cerrarla o sacarla del board para reducir ruido. El board debe mostrar el camino
de producto, no todos los detalles internos posibles.
