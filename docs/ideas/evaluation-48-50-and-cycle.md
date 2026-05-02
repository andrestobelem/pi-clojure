# Evaluación de backlog #48-#50 y ciclo de agentes

Fecha: 2026-05-02.

Alcance: evaluación de las issues #48, #49 y #50, observación en modo lectura de
los worktrees activos #48/#49, lectura de transcripts dogfood y mejora del ciclo
de agentes. No se implementó código ni se editaron `src/` o `test/`.

## Estado observado

- Worktree actual: `review/backlog-48-50-y-ciclo`, limpio al iniciar.
- #48 `story/48-listar-salas-actividad`: worktree vecino limpio, sin commits ni
  cambios locales por encima de `main` observado. En el Project está `In
  Progress` y `Foco: Ahora`, pero aún no hay evidencia local de ciclo Red.
- #49 `story/49-validar-estructura-mensajes`: worktree vecino limpio, sin commits
  ni cambios locales por encima de `main` observado. En el Project está `In
  Progress` y `Foco: Ahora`, pero aún no hay evidencia local de ciclo Red.
- #50 está abierta en `Todo`. Conviene mantenerla fuera de implementación hasta
  integrar o pausar alguno de los dos streams activos.

## Lectura dogfood relevante

- `docs/ideas/agent-roundtable-live.md` consolidó el valor de una demo dogfood
  reproducible, serializada y auditable.
- `docs/ideas/agent-roundtable-stories-live.md` originó el bloque de metadata e
  idempotencia ya integrado antes de esta evaluación.
- `docs/ideas/agent-roundtable-more-stories-live.md` originó #48, #49 y #50:
  descubrimiento de salas, validación estructural de mensajes backlog y errores
  seguros para no participantes.

## Checklist de integración recomendado

Checklist operativo canonicalizado:
[`../multi-agent-cycle-checklist.md`](../multi-agent-cycle-checklist.md).

### Antes de implementar cada story

- [ ] Confirmar `git status --short --branch` limpio en el worktree de la story.
- [ ] Confirmar que no haya más de 1-2 issues en `In Progress`.
- [ ] Confirmar Project: `Status`, `Foco` y `Canvas` coherentes con la story.
- [ ] Escribir o ajustar primero un test de aceptación rojo.
- [ ] Evitar tocar archivos que esté editando otro worktree activo.

### Antes de pedir integración

- [ ] Verificar que la story tenga commits atómicos con Conventional Commits.
- [ ] Correr `clj-kondo --lint src test`.
- [ ] Correr `clojure -M:test`.
- [ ] Si se editó Markdown, correr
  `npx markdownlint-cli2 '**/*.md' '#node_modules'`.
- [ ] Revisar diff contra `main` y confirmar que no mezcla otra story.
- [ ] Actualizar README/docs solo si el nuevo comportamiento necesita guía de
  usuario.

### Al integrar a `main`

- [ ] Rebase/merge corto desde `main` sin resolver cambios ajenos a la story.
- [ ] Repetir checks en el worktree de integración si hubo conflictos o rebase.
- [ ] Pushear `main`.
- [ ] Marcar la issue como `Done` solo cuando el cambio esté integrado.
- [ ] Borrar branch local/remota de la story y dejar worktree limpio.

## Riesgos por story

### #48 Listar salas con actividad básica

- Riesgo de producto: `rooms` global puede exponer nombres de salas si luego se
  decide que el descubrimiento debe estar filtrado por actor.
- Riesgo de formato: tests demasiado acoplados a copy pueden bloquear mejoras de
  UX. Preferir tokens estables: nombre, `mensajes: N`, `participantes: N`.
- Riesgo de coordinación: toca CLI y lectura de estado, zona que #49 también
  puede tocar para agregar comandos.

### #49 Validar estructura Markdown dogfood/backlog

- Riesgo de duplicar validación Markdown base en vez de componer sobre el comando
  existente.
- Riesgo de alcance: archivo/stdin/inline pueden convertirse en tres features;
  elegir un contrato mínimo y extender luego.
- Riesgo de UX: errores demasiado genéricos no ayudan al agente a corregir el
  mensaje antes de publicarlo.

### #50 Errores seguros para no participantes

- Riesgo de falsa seguridad: cubrir `send` y `show` pero olvidar `export`, que
  también lee contenido de sala.
- Riesgo de filtrado: mejorar mensajes de error revelando handles de
  participantes, nombres de mensajes o contenido existente.
- Riesgo de redundancia: si el comportamiento ya existe, el valor debe quedar en
  tests de caracterización y documentación, no en cambios innecesarios.

## Refinamiento recomendado para #50

Mantener #50 como historia de verificación/regresión, pero ampliar sus criterios
para cubrir `export` además de `send` y `show`. La historia debería declarar que
un lector no participante no puede extraer el transcript por ninguna superficie
CLI de lectura.

Criterios refinados sugeridos:

- `send` rechaza usuarios existentes pero no unidos a la sala con error claro y
  exit code no cero.
- `show` rechaza lectores no participantes con error claro y exit code no cero.
- `export` rechaza lectores no participantes con error claro y exit code no cero.
- El error no revela contenido de la sala, lista de participantes ni cuerpos de
  mensajes.
- El flujo documenta que `join` es el paso explícito para participar.
- Si la suite ya cubre todo, cerrar con test/documentación de verificación sin
  cambiar comportamiento.

Primer test rojo refinado: crear `alice` y `mallory`, unir solo a `alice`, enviar
un mensaje sensible de `alice` y ejecutar `send`, `show` y `export` como
`mallory`. Los tres caminos deben fallar sin persistir mensajes nuevos, con
stdout vacío o no sensible y stderr accionable sin filtrar datos de sala.

## Mejora propuesta del ciclo de agentes

Crear una historia de producto/proceso para incorporar un checklist operativo de
ciclo multiagente: crear historias desde transcripts, implementar en worktrees,
evaluar integración, marcar checklists del issue y cerrar. Aporta valor porque
las issues #48/#49 están en `In Progress` sin señal local de Red, y #50 estaba en
`Foco: Ahora` aunque ya hay dos streams activos.

Slice sugerido:

- Documento o script liviano con etapas: `story intake`, `red`, `green`,
  `refactor`, `integration review`, `project update`, `close`.
- Checklist reusable para pegar en issues o validar antes de cerrar.
- Regla explícita: una story no pasa a `In Progress` sin worktree/branch y primer
  test rojo identificado; una story no pasa a `Done` sin checks y commit/merge.
- Guía de Project: máximo dos `In Progress`, `Foco: Ahora` solo para streams
  activos, backlog siguiente en `Foco: Siguiente` o `Después`.

## Orden sugerido

1. Mantener #48 y #49 como los dos streams activos solo si empiezan ciclo Red
   pronto; si no, pausar uno para liberar foco.
2. Mover #50 a `Foco: Siguiente` y refinarla con cobertura de `export`.
3. Integrar #48/#49 en cortes pequeños, evitando que ambos cambien el parser CLI
   a la vez sin coordinación.
4. Ejecutar #50 como caracterización de seguridad antes de ampliar más UX de
   lectura/exportación.
5. Agregar la mejora del ciclo como story de proceso con `Foco: Después` si el
   tablero ya está saturado.

## Checks

- Pendiente tras editar este documento: `npx markdownlint-cli2 '**/*.md'
  '#node_modules'`.
