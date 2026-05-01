# Sala personal por usuario

Cada usuario debería tener una sala personal privada creada automáticamente.

Esta sala no es solo un chat con uno mismo: funciona como inbox, cuaderno
Markdown, diario de trabajo y espacio personal de conversación. Es el lugar donde
una idea puede nacer antes de convertirse en documento, propuesta o conversación
compartida.

## Hipótesis

El conocimiento muchas veces empieza de forma privada o incompleta. Antes de
compartir una idea con un equipo, una persona puede necesitar capturarla,
ordenarla, conversar con una IA, revisar alternativas o convertirla en un texto
más claro.

La sala personal permite ese flujo sin salir del mismo producto.

```text
idea privada
  -> sala personal
  -> documento Markdown
  -> sala compartida
  -> discusión
  -> decisión versionada
```

## Comportamiento inicial

- Se crea automáticamente cuando se crea un usuario.
- Es privada por defecto.
- Tiene un único propietario.
- Acepta mensajes Markdown como cualquier otra sala.
- Se puede exportar como Markdown.
- Puede tener snapshots o commits como las salas compartidas.

## Usos posibles

- Notas rápidas.
- Diario de trabajo.
- Inbox personal.
- Borradores de propuestas.
- Conversaciones con agentes o IA.
- Captura de enlaces y citas.
- Preparación de decisiones antes de compartirlas.
- Registro personal de aprendizaje.

## Modelo conceptual

Podemos modelar las salas con un tipo:

```clojure
{:room/type :room.type/personal}
{:room/type :room.type/shared}
```

Una sala personal debería tener:

```clojure
{:room/type :room.type/personal
 :room/owner-id user-id
 :room/visibility :room.visibility/private}
```

## Preguntas abiertas

- ¿La sala personal puede invitar temporalmente a otras personas?
- ¿Puede una sección de la sala personal publicarse o moverse a una sala
  compartida?
- ¿Conviene que funcione también como inbox de menciones?
- ¿Cómo se versionan los cambios cuando una idea pasa de privada a compartida?
- ¿Una sala personal puede tener agentes o bots permanentes?
