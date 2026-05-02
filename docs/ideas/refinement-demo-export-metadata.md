# Refinamiento de demo y metadatos de exportación

Fecha: 2026-05-02

Alcance de esta revisión: historias #34 y #37. No cambia código ni tests. El
objetivo es preparar criterios, dependencias, riesgos de conflicto con #35 y #36,
y cortes de implementación para ciclos TDD posteriores.

## Contexto revisado

- `git status --short --branch`: branch `review/refinar-34-37`, sin cambios
  iniciales.
- #34: guion de demo end-to-end reproducible, en `Todo`.
- #37: exportaciones con autor y metadatos mínimos, en `Todo`.
- #35 y #36 están `In Progress` en el Project, por lo que #34 y #37 no deberían
  moverse ni implementarse hasta coordinar interfaces compartidas.
- `README.md` define TDD, trunk-based development, uso de worktrees y checks.
- `docs/ideas/next-backlog.md` ubica #34 y #37 en `Siguiente`.
- `docs/ideas/refinement-export-demo.md` ya refina #34 junto con #31 y marca que
  la demo depende de la interfaz final de exportación a archivo.
- `docs/ideas/refinement-markdown-participation.md` refina #35 y #36, y marca
  que validación Markdown, auditoría de participación y exportación no deben
  mezclarse.

## Historia #34: guion de demo end-to-end reproducible

### Objetivo refinado de #34

Proveer una demo ejecutable y documentada del flujo MVP-1: crear usuarios y
salas, enviar Markdown seguro, ver conversación ordenada y exportar un documento
Markdown sin que el evaluador conozca comandos internos ni dependa del estado
local de desarrollo.

### Criterios de aceptación propuestos para #34

- Existe un guion reproducible, por ejemplo `scripts/demo-export-chat.sh`, que
  puede ejecutarse desde un checkout limpio.
- El guion usa un estado temporal o aislado y no lee ni modifica `.pi-chat.edn`
  del desarrollador.
- El guion crea al menos dos usuarios humanos y una sala compartida.
- El guion demuestra una sala personal existente o creada por el flujo actual,
  sin exigir reglas nuevas de permisos.
- El guion envía al menos dos mensajes Markdown válidos a la sala compartida.
- El guion muestra la conversación ordenada antes de exportar.
- El guion exporta a un archivo Markdown usando la interfaz estable de #31 si ya
  está integrada; si #31 no está integrada, la historia debe esperar o limitarse
  a documentación no ejecutable.
- El guion verifica que el archivo exportado existe y contiene fragmentos
  esperados de los mensajes enviados.
- El guion falla con exit code distinto de cero si cualquier comando falla o si
  la verificación del artefacto no se cumple.
- La documentación asociada incluye comandos copiables y salida representativa,
  sin rutas absolutas locales ni datos de una ejecución privada.

### Casos de test o verificación sugeridos para #34

1. Smoke test: ejecutar el guion en un directorio temporal y esperar exit code 0.
2. Aislamiento: correr el guion dos veces no falla por estado previo ni deja
   basura en el repo.
3. Artefacto: el archivo exportado contiene el título o identificador de sala y
   al menos los mensajes enviados por la demo.
4. Falla visible: si un comando de la CLI devuelve error, `set -euo pipefail` o
   una verificación equivalente detiene el guion.
5. Documentación: el ejemplo documentado coincide con el orden lógico del guion,
   aunque la salida exacta pueda resumirse como representativa.

### Dependencias de #34

- Depende de #31 para exportar a archivo si la demo debe producir un `.md` como
  artefacto. No conviene codificar una interfaz temporal.
- Se beneficia de #35 para validar Markdown antes de enviar, pero no debería
  depender de ese comando salvo que la demo incluya una sección explícita de
  preflight. La demo puede usar mensajes ya conocidos como válidos.
- No depende de #36. Los eventos de `join`/`leave` no deberían ser requisito de
  la demo mientras el objetivo sea mostrar chat y exportación.
- Depende de contratos estables de CLI para creación de usuarios, salas, envío,
  lectura y exportación.

### Riesgos de conflicto de #34 con #35 y #36

- Con #35: riesgo medio sobre parsing de CLI, mensajes de error y canales de
  salida si la demo captura stdout/stderr o si incorpora `validate-markdown`.
  Mitigación: mantener el preflight fuera del camino crítico de #34 o esperar a
  que #35 estabilice el formato de éxito/error.
- Con #36: riesgo bajo si la demo no inspecciona auditoría. Riesgo medio si el
  guion usa `join`/`leave` y #36 cambia su salida visible o su idempotencia.
  Mitigación: verificar resultados observables de chat/export, no detalles de
  eventos internos.
- Con documentación: #34 puede tocar README o docs de demo; coordinar para no
  pisar refinamientos o ejemplos que #35 agregue sobre validación.

### Slicing recomendado para #34

1. **#34a smoke ejecutable aislado**: script temporal, mensajes Markdown válidos,
   exportación a archivo y verificación mínima de contenido.
2. **#34b documentación de demo**: comandos copiables y salida representativa en
   README o en un documento de demo específico.
3. **#34c preflight opcional**: incorporar `validate-markdown` solo después de
   integrar #35 y sin convertirlo en dependencia del flujo principal.
4. **#34d endurecimiento**: parametrizar directorio temporal, limpiar artefactos
   y mejorar legibilidad de logs.

## Historia #37: exportaciones con autor y metadatos mínimos

### Objetivo refinado de #37

Hacer que el Markdown exportado sea comprensible fuera de la CLI: debe indicar
qué sala se exportó, de qué tipo es y quién escribió cada mensaje, preservando
el cuerpo Markdown original sin renderizarlo ni normalizarlo.

### Criterios de aceptación propuestos para #37

- La exportación incluye un título estable derivado de la sala, por ejemplo un
  encabezado Markdown de nivel 1 con el nombre o identificador público.
- La exportación incluye el tipo de sala con una etiqueta estable, por ejemplo
  `personal` o `shared`, sin exponer detalles internos innecesarios.
- Cada mensaje incluye su secuencia visible en el orden de conversación.
- Cada mensaje incluye el handle del autor.
- Si el autor es agente, se indica de forma simple y estable, por ejemplo
  `agente` o `agent`, según el lenguaje ya usado por el dominio.
- El cuerpo Markdown original de cada mensaje se conserva byte a byte en cuanto
  a contenido lógico: no se renderiza a HTML, no se sanitiza de nuevo y no se
  reescribe el énfasis, listas o bloques de código.
- La salida es Markdown determinista y testeable para el mismo estado de entrada.
- Los metadatos de mensajes no se confunden con el cuerpo del mensaje; usar un
  formato separador estable, como encabezados por mensaje o líneas de metadata.
- La historia no agrega exportación de eventos de participación, snapshots,
  permisos nuevos ni escritura a archivo si #31 ya cubre ese destino.

### Casos de test sugeridos para #37

1. Red dominio: exportar una sala compartida con dos mensajes incluye título,
   tipo de sala, secuencias y handles.
2. Red dominio: exportar una sala personal incluye tipo `personal` o etiqueta
   equivalente fijada por test.
3. Red dominio: un mensaje de agente muestra una marca simple de agente junto al
   autor o metadata del mensaje.
4. Red regresión Markdown: un cuerpo con `**énfasis**`, lista y bloque de código
   aparece como Markdown, no como HTML renderizado.
5. Red estabilidad: dos exportaciones del mismo estado producen exactamente el
   mismo string.
6. Red integración CLI si aplica: `export` por stdout y `export --output` usan el
   mismo formato enriquecido cuando #31 está integrado.

### Dependencias de #37

- Depende de que el modelo de mensajes pueda resolver secuencia, autor y handle
  sin acoplarse a detalles de persistencia.
- Depende de que el modelo de sala exponga o derive un tipo mínimo (`personal` o
  compartida) de forma estable.
- Se relaciona con #31: #37 cambia el formato del documento exportado; #31 cambia
  el destino. Conviene integrar o rebasar sobre #31 para que ambos caminos usen
  la misma función de render Markdown.
- No debería depender de #34. La demo puede adaptarse al nuevo formato cuando
  #37 esté integrado, pero #37 debe poder probarse sin ejecutar la demo completa.
- No depende de #35 ni de nuevas reglas Markdown; solo preserva cuerpos que ya
  pasaron la validación existente.
- No depende de #36 y no debe exportar eventos de participación en esta historia.

### Riesgos de conflicto de #37 con #35 y #36

- Con #35: riesgo bajo a medio si #35 refactoriza la validación o representación
  del cuerpo Markdown. Mitigación: #37 debe tratar el cuerpo como dato ya válido
  y no invocar validación durante exportación.
- Con #35: riesgo de alcance si se intenta normalizar Markdown para hacerlo más
  prolijo en export. Mitigación: test explícito de preservación del cuerpo.
- Con #36: riesgo medio si #36 agrega un event log al mismo estado que export lee.
  Mitigación: filtrar explícitamente mensajes de chat y agregar test de que
  eventos de participación no aparecen en el documento exportado.
- Con #36: riesgo de orden si se reutiliza una secuencia global de eventos para
  mensajes. Mitigación: decidir por test si la secuencia exportada es la
  secuencia visible de mensajes, no necesariamente el índice del event log.

### Slicing recomendado para #37

1. **#37a encabezado de documento**: título y tipo de sala con formato estable.
2. **#37b metadata por mensaje humano**: secuencia visible y handle del autor para
   mensajes existentes.
3. **#37c marca de agente**: cubrir usuarios agente sin cambiar el formato del
   cuerpo.
4. **#37d regresiones de preservación**: tests de Markdown original, estabilidad
   byte a byte y no contaminación con eventos de participación.

## Orden sugerido y coordinación

1. Terminar o estabilizar #31 antes de implementar #34, porque la demo necesita
   una interfaz final de exportación a archivo.
2. Implementar #37 antes o cerca de #34 si se quiere que la demo muestre el
   formato final enriquecido; si #34 avanza primero, sus verificaciones deben ser
   tolerantes al futuro agregado de metadatos.
3. No bloquear #37 por #35: coordinar solo si ambas ramas tocan el mismo parser o
   tests de Markdown.
4. No bloquear #34 por #36: evitar que la demo dependa de eventos de
   participación mientras #36 esté activa.
5. Si #36 introduce event log compartido, revalidar #37 para asegurar que export
   siga seleccionando mensajes y no eventos.

## Checklist para agentes implementadores

- Empezar cada historia con `git status --short --branch`.
- Crear branch por issue desde `main` usando `gh issue develop`.
- Escribir primero tests o smoke checks rojos.
- Mantener #34 como demo de flujo, no como feature nueva de dominio.
- Mantener #37 como cambio de formato Markdown, no como cambio de destino,
  permisos o auditoría.
- No editar #34/#37 en paralelo con #35/#36 sobre los mismos archivos sin
  coordinar.
- Después de tocar Clojure: `clj-kondo --lint src test` y `clojure -M:test`.
- Después de tocar Markdown: `npx markdownlint-cli2 '**/*.md' '#node_modules'`.
