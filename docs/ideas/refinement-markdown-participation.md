# Refinamiento de validación Markdown y auditoría de participación

Fecha: 2026-05-02

Alcance de esta revisión: historias #35 y #36. No cambia código ni tests. El objetivo es dejar criterios, dependencias, riesgos de conflicto y cortes de implementación claros para ciclos TDD posteriores.

## Contexto revisado

- `git status --short --branch`: branch `review/refinar-35-36`, sin cambios iniciales.
- #35: validar Markdown antes de enviar.
- #36: auditar entradas y salidas de una sala.
- #31 y #33 están `In Progress` en worktrees separados.
- `README.md` define TDD, trunk-based development, uso de worktrees y checks.
- `docs/ideas/next-backlog.md` ubica #35 y #36 en `Siguiente`.
- `docs/ideas/refinement-export-demo.md` refina #31 y #34, y recomienda no mezclar exportación con cambios de formato o permisos.

## Historia #35: validar Markdown antes de enviar

### Objetivo refinado de #35

Exponer un comando de preflight para scripts o integraciones que use exactamente las mismas reglas de validación Markdown que `send`, sin crear mensajes, eventos ni archivos de estado por el solo hecho de validar.

### Criterios de aceptación propuestos para #35

- `validate-markdown "Hola **mundo**"` termina exitosamente e imprime `Markdown válido` o una confirmación equivalente estable.
- `validate-markdown "<b>Hola</b>"` termina con error visible y código estable, por ejemplo `markdown/raw-html`.
- El error estructurado incluye al menos código, campo lógico (`message.body` o equivalente) y mensaje accionable.
- Ejecutar `validate-markdown` no crea el archivo de estado si no existe.
- Ejecutar `validate-markdown` no modifica el archivo de estado si ya existe.
- La validación reutiliza el resultado de dominio de `send`; no duplica listas de reglas ni mensajes de error en la CLI.
- La salida de éxito no contiene el Markdown original completo, para evitar eco accidental de contenido sensible en scripts.
- La historia no agrega lint no bloqueante, normalización, render HTML ni política nueva de links/imágenes.

### Casos de test sugeridos para #35

1. Red CLI: Markdown seguro devuelve éxito y salida estable.
2. Red CLI: HTML crudo devuelve error estructurado y exit code distinto de cero si el harness permite comprobarlo.
3. Red CLI/regresión: validar sin estado previo no crea archivo de estado.
4. Red CLI/regresión: validar con estado existente conserva contenido byte a byte.
5. Red dominio/servicio: `validate-markdown` y `send` comparten la misma función o resultado de validación para el mismo input inseguro.
6. Red CLI: argumentos incompletos o vacíos fallan con error de uso, sin tocar estado.

### Decisiones a fijar durante TDD de #35

- Nombre exacto del comando: mantener `validate-markdown` para coincidir con la issue.
- Canal de salida: se recomienda éxito por stdout y errores por el mismo canal/estilo que haya fijado la historia de errores CLI.
- Semántica de input vacío: decidir por test si `""` es Markdown válido vacío o error de uso/dominio.
- Formato mínimo de error: si ya existe contrato de errores accionables, reutilizarlo sin inventar otro formato.

### Dependencias de #35

- Depende de que exista una validación Markdown de dominio invocable sin persistir mensajes.
- Conviene integrar o al menos revisar la historia de errores accionables antes de cerrar #35, para alinear códigos, campos y exit codes.
- No depende de #31 salvo por coordinación sobre parsing y salida CLI.
- No depende de #36; validar Markdown no debe registrar participación ni eventos.

### Riesgos de conflicto de #35

- Con #31: riesgo medio si ambas historias editan parsing de comandos, ayuda de CLI o pruebas sobre stdout/stderr. Coordinar orden de flags y mantener el comando de exportación aislado.
- Con #33: riesgo bajo a medio si #33 refactoriza `send` para idempotencia y toca el mismo camino de validación. #35 debe llamar la validación común antes de cualquier lógica de `client-txn-id`, sin crear mensajes ni eventos.
- Con futuras políticas Markdown: riesgo de alcance. No agregar reglas nuevas en #35; solo exponer las existentes.
- Con tests de estado: riesgo de fragilidad si los tests dependen de rutas globales. Usar estado temporal o fixtures aislados.

### Slicing recomendado para #35

La historia #35 puede implementarse en una sola rebanada si la validación ya está separada. Si aparece fricción, cortar en:

1. **#35a preflight puro**: comando `validate-markdown`, éxito/error básico y garantía de no tocar estado.
2. **#35b errores alineados**: código estable, campo afectado y mensajes consistentes con el contrato de errores CLI.
3. **#35c endurecimiento de regresión**: casos de input vacío, ayuda de uso y pruebas de no duplicar reglas.

No incluir en #35 warnings, sanitización, links/imágenes, exportación ni cambios de permisos.

## Historia #36: auditar entradas y salidas de una sala

### Objetivo refinado de #36

Registrar eventos de participación para reconstruir cuándo un actor entró o salió de una sala, preservando idempotencia operacional: comandos repetidos no deben inflar la auditoría.

### Criterios de aceptación propuestos para #36

- `join` exitoso registra exactamente un evento `:participation/joined`.
- `leave` exitoso registra exactamente un evento `:participation/left`.
- Repetir `join` sobre una participación ya activa no duplica eventos.
- Repetir `leave` sobre una participación inactiva no duplica eventos; el comportamiento visible debe fijarse por test como no-op exitoso o error explícito.
- Cada evento conserva `room-id`, `actor-id` y tipo de evento.
- Si el dominio ya maneja secuencia o timestamp lógico de eventos, los eventos de participación respetan ese mecanismo en vez de crear otro.
- Los eventos de participación no aparecen como mensajes de chat ni cambian el Markdown exportado, salvo que una historia futura lo pida explícitamente.
- La auditoría no cambia las reglas de acceso ni la lista de participantes activos más allá del comportamiento actual de `join`/`leave`.

### Casos de test sugeridos para #36

1. Red dominio: `join` de un actor nuevo agrega evento `:participation/joined` con ids esperados.
2. Red dominio: segundo `join` del mismo actor activo conserva una sola entrada de auditoría.
3. Red dominio: `leave` de actor activo agrega evento `:participation/left` con ids esperados.
4. Red dominio: segundo `leave` de actor inactivo conserva la auditoría sin duplicar; fijar por test el resultado visible.
5. Red integración/CLI si aplica: luego de `join` y `leave`, el estado persistido contiene eventos de participación.
6. Red regresión: `export` o `show` no mezclan eventos de participación como mensajes.

### Decisiones a fijar durante TDD de #36

- Resultado de `leave` repetido: elegir por test entre no-op idempotente o error explícito. Se recomienda error explícito si el comando ya comunica fallas de dominio; no-op si la operación se modela como convergencia de estado.
- Nombre y ubicación de la colección de eventos: reutilizar el historial/event log existente si lo hay; evitar crear una estructura paralela no consultable.
- Actor del evento: confirmar si `actor-id` es quien ejecuta la operación o el usuario afectado. Para esta historia, mantenerlos iguales salvo que ya exista administración delegada.
- Orden relativo con cambios de estado: registrar evento solo si la transición de participación se aplicó efectivamente.

### Dependencias de #36

- Depende de que `join` y `leave` tengan una representación clara de participación activa/inactiva.
- Se relaciona con la historia de participantes activos del backlog: conviene no duplicar lógica, pero #36 puede avanzar si se limita a eventos.
- No depende de #35; la validación Markdown no debe conocer eventos de participación.
- No depende de #31; exportar Markdown no debería incluir eventos de participación en esta historia.

### Riesgos de conflicto de #36

- Con #31: riesgo bajo si #36 no cambia el formato exportado. Riesgo medio si ambas historias tocan la representación global del estado que `export` lee. Agregar tests de regresión para que eventos no contaminen la exportación.
- Con #33: riesgo medio si #33 introduce o refactoriza un event log para mensajes idempotentes. Alinear nombres, secuencia y deduplicación de eventos para no tener dos modelos de auditoría.
- Con historias de participantes activos: riesgo medio sobre `join`/`leave`. Coordinar si otra rama cambia las mismas funciones de dominio.
- Con persistencia futura: riesgo de sobrediseño. No introducir contrato Dolt ni migraciones; mantenerlo en el store actual.

### Slicing recomendado para #36

La historia #36 es candidata a dos rebanadas si el event log actual no está preparado:

1. **#36a evento de join**: registrar `:participation/joined`, deduplicar join activo y persistir ids mínimos.
2. **#36b evento de leave**: registrar `:participation/left`, fijar leave repetido y cubrir no contaminación de mensajes/export.
3. **#36c consulta interna de auditoría**: solo si los tests necesitan una API legible para inspeccionar eventos sin acoplarse al mapa de estado completo.

No incluir en #36 UI de auditoría, comando `history`, cambios de permisos, snapshots ni exportación de eventos.

## Orden sugerido y coordinación

1. Terminar o estabilizar #33 antes de refactorizar caminos compartidos de envío/eventos.
2. Implementar #35 si la CLI no está siendo editada intensivamente por #31; si #31 sigue activa, esperar o coordinar para evitar conflictos en parsing/salida.
3. Implementar #36 cuando #33 haya fijado el modelo de idempotencia/eventos, o limitar #36 estrictamente a participación si #33 no toca ese sector.
4. Mantener #31, #35 y #36 independientes: exportación a archivo, preflight Markdown y auditoría de participación no deben mezclarse en un mismo commit.

## Checklist para agentes implementadores

- Empezar cada historia con `git status --short --branch`.
- Crear branch por issue desde `main` usando `gh issue develop`.
- Escribir primero tests o ejemplos rojos.
- No editar #35 y #31 en paralelo sobre el mismo parser CLI sin coordinar.
- No editar #36 y #33 en paralelo sobre el mismo event log sin coordinar.
- Después de tocar Clojure: `clj-kondo --lint src test` y `clojure -M:test`.
- Después de tocar Markdown: `npx markdownlint-cli2 '**/*.md' '#node_modules'`.
