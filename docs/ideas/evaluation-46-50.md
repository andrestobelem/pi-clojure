# Evaluación de backlog #46-#50

Fecha: 2026-05-02.

Alcance: evaluación/slicing de las issues #46, #47, #48, #49 y #50 a partir de
las issues de GitHub, los transcripts dogfood `agent-roundtable-*-live.md` y los
worktrees vecinos en modo lectura. No se implementó código ni se editaron
`src/` o `test/`.

## Estado observado

- Worktree actual: `review/evaluar-46-50`, limpio al iniciar.
- #46 `story/46-export-metadata-auditable`: worktree vecino con cambios no
  commiteados solo en `test/pi_clojure/cli_test.clj`; agrega un test rojo para
  `export --with-meta` y verifica que el export por defecto no incluya
  `client-txn-id`. No parece listo para checks de integración porque no hay
  implementación ni commit.
- #47 `story/47-send-idempotency-feedback`: worktree vecino con cambios no
  commiteados solo en `test/pi_clojure/cli_test.clj`; agrega un test rojo para
  distinguir creación vs reutilización y conservar un único mensaje. No parece
  listo para checks de integración porque no hay implementación ni commit.
- #48, #49 y #50 no tienen worktree activo observado.

## Lectura de transcripts

- `agent-roundtable-stories-live.md` concentra dos fricciones separables:
  metadata auditable en lectura/exportación y feedback de idempotencia en
  `send`.
- `agent-roundtable-more-stories-live.md` aporta tres historias nuevas:
  descubrimiento de salas, validación estructural dogfood/backlog y verificación
  explícita de errores de acceso para no participantes.
- `agent-roundtable-metadata-live.md` confirma que la necesidad original era
  auditar orden, autor, timestamp y `client-txn-id`, pero también expone una
  fricción de quoting ya cubierta por una historia previa.

## Slicing recomendado

### #46 Exportar transcripts con metadata auditable

Slice recomendado: agregar un modo explícito `export --with-meta` que reutilice
la información ya visible en `show --with-meta`, sin alterar `export` por
defecto ni `show`.

Criterios de corte:

- Mantener el documento por defecto byte-compatible salvo cambios ya aceptados.
- Incluir por mensaje: autor, orden, timestamp y `client-txn-id` si existe.
- Preservar el cuerpo Markdown sin escaparlo ni revalidarlo durante export.
- Documentar un ejemplo mínimo de uso, idealmente cerca de la demo dogfood.

Evitar incluir en #46:

- Cambios en idempotencia o mensajes de `send`.
- Rediseño general del formato de `show --with-meta`.
- Solución de concurrencia del state file.

Primer test recomendado: el test ya bosquejado en el worktree #46 es buen primer
rojo: dos mensajes con `client-txn-id` conocidos, `export --with-meta`, metadata
en orden y Markdown preservado; además, regresión de que `export` sin flag no
incluye `client-txn-id`.

### #47 Feedback de creación vs reutilización en `send`

Slice recomendado: hacer que el camino de `send` conozca si el resultado fue
creación nueva o retry idempotente compatible y lo exprese en stdout estable.

Criterios de corte:

- No duplicar mensajes al reenviar mismo handle/sala/cuerpo/`client-txn-id`.
- Primer envío: salida estable que indique creación o envío nuevo.
- Retry compatible: salida estable que indique reutilización/idempotencia.
- Incluir el `client-txn-id` usado en la salida.
- Mantener conflicto actual si el mismo `client-txn-id` apunta a otro cuerpo o
  sala.

Evitar incluir en #47:

- Auditoría de export/show más allá de lo ya existente.
- Validación adicional de `client-txn-id` vacío si no entra en el corte mínimo;
  si se aborda, hacerlo como commit separado.

Primer test recomendado: el test ya bosquejado en el worktree #47 es buen primer
rojo. Como ajuste de diseño, conviene no acoplarse a una frase demasiado larga:
verificar palabras estables como `creado`, `reutilizado` e `client-txn-id` puede
hacer el contrato menos frágil.

### #48 Listar salas con actividad básica

Slice recomendado: un comando nuevo `rooms` sin usuario actor, orientado a
descubrimiento operacional de demo.

Criterios de corte:

- `clojure -M:chat rooms` lista salas existentes ordenadas por nombre o título.
- Cada sala muestra conteo de mensajes.
- Cada sala muestra conteo de participantes activos.
- Estado sin salas: salida clara, exit code 0 y sin timestamps volátiles.

Primer test recomendado: CLI con state temporal que crea dos salas, tres handles
y participaciones/mensajes distribuidos; `rooms` debe mostrar ambas salas en
orden estable con `mensajes: N` y `participantes: N`. Agregar un segundo test
pequeño para estado vacío exitoso.

### #49 Validar estructura Markdown dogfood/backlog

Slice recomendado: extender la validación existente con un modo/comando explícito
de estructura dogfood, por ejemplo `validate-backlog-message` o
`validate-markdown --template backlog`, sin modificar estado.

Criterios de corte:

- Reutilizar la validación Markdown base actual y sumar chequeo de encabezados
  requeridos.
- Requerir secciones mínimas alineadas con transcripts: `### Fricción observada`,
  `### Historia candidata`, `### Criterios de aceptación` y
  `### Primer test rojo sugerido`.
- Error accionable: indicar encabezado faltante.
- No cargar ni guardar state; no enviar mensajes ni consumir `client-txn-id`.
- Si se agrega input por archivo/stdin, hacerlo en el menor slice posible y con
  contrato claro.

Primer test recomendado: mensaje sin `### Historia candidata`; ejecutar la
validación estructural, esperar exit code distinto de cero, stderr con el
encabezado faltante y state inexistente o sin cambios.

### #50 Verificar errores seguros para no participantes

Slice recomendado: comenzar como historia de caracterización/regresión. El dominio
actual ya exige participante activo para `send`, `show` y `export`; la brecha
probable está en tests CLI/documentación y en calidad del mensaje.

Criterios de corte:

- `send` de usuario existente no unido falla con exit code no cero.
- `show` de usuario existente no unido falla con exit code no cero.
- El error no lista participantes ni contenido de mensajes.
- README o demo documenta `join` como paso explícito antes de participar.

Primer test recomendado: crear `alice` y `mallory`, sala con `alice` unida y un
mensaje privado de contexto; `mallory` ejecuta `send` y `show`. Verificar exit
code 1, stderr con participación requerida, stdout vacío, sin persistir el nuevo
mensaje y sin filtrar el cuerpo existente ni handles de participantes.

## Solapamientos y dependencias

- #46 y #47 se solapan conceptualmente por `client-txn-id`, pero pueden
  integrarse separados si #46 solo toca exportación y #47 solo toca resultado de
  `send`.
- #46 puede editar `export-options`, `export-room-markdown` o funciones de
  formato; #48 probablemente toque parsing de comandos y lectura de salas. Riesgo
  bajo-medio en `cli.clj`.
- #47 probablemente requiere cambiar el valor de retorno de `send-message!` o
  agregar metadata de resultado. Eso puede impactar tests existentes de warnings
  y retry idempotente.
- #49 toca la zona de validación Markdown y CLI. Evitar mezclarlo con #47 si #47
  decide validar `client-txn-id` o reordenar el flujo de `send`.
- #50 toca autorización y mensajes de error; no debería bloquear #46/#47, pero
  puede compartir helpers de errores CLI.

## Orden de integración recomendado

1. #47, porque fija el contrato observable de idempotencia y reduce ambigüedad
   para demos y retries.
2. #46, porque aprovecha la metadata existente y complementa #47 sin cambiar el
   export por defecto.
3. #50, como caracterización de seguridad antes de sumar más UX de descubrimiento.
4. #48, después de estabilizar acceso/participación para decidir si `rooms` es
   global o filtrado por actor en futuras historias.
5. #49, al final o en paralelo con bajo riesgo si se limita a comando nuevo; no
   debe interferir con `send` salvo reutilizar validación base.

## Riesgos principales

- Fijar formatos demasiado rígidos en tests CLI y dificultar refactors de copy.
- Mezclar `show --with-meta` y `export --with-meta` hasta cambiar el export por
  defecto accidentalmente.
- Cambiar la firma de dominio para #47 sin actualizar warnings ni eventos.
- Que `rooms` revele más información de la deseada si luego se decide filtrar por
  participante/actor.
- Duplicar reglas Markdown en #49 en vez de componer sobre la validación base.
- Mejorar mensajes de error de #50 revelando sala, participantes o contenido.

## Checks

No corrí checks en los worktrees #46/#47 porque ambos se observan en fase roja
con cambios no commiteados solo de tests y sin implementación. Cuando tengan
implementación candidata o commits listos, correr en sus worktrees:

```sh
clj-kondo --lint src test
clojure -M:test
```
