# Evaluación de historias #40 y #41

Fecha: 2026-05-02

Rol: evaluator/backlog gardener. Alcance: no implementar código, no editar `src/` ni
`test/`, no cerrar issues ni mover Project items.

## Insumos revisados

- `git status --short --branch` en este worktree: limpio en
  `review/evaluar-40-41`.
- `gh issue view 40`: abierta, en `In Progress`, lint Markdown no bloqueante.
- `gh issue view 41`: abierta, en `In Progress`, snapshot simple de sala.
- `docs/ideas/refinement-lint-snapshot.md`.
- `docs/research/2026-05-01-agent-patterns-backlog-and-implementation.md`.
- Worktrees vecinos observados en modo lectura:
  - `/Users/andrestobelem/ws/at/pi-clojure-40-lint-no-bloqueante`.
  - `/Users/andrestobelem/ws/at/pi-clojure-41-snapshot-simple-sala`.

## Checklist de evaluación

### Común

- [ ] La branch de la story tiene cambios atómicos y dentro de alcance.
- [ ] No hay archivos no relacionados ni documentación accidental.
- [ ] La historia conserva TDD visible: test rojo concreto antes o junto con la
      implementación mínima.
- [ ] `clj-kondo --lint src test` pasa en el worktree de la story.
- [ ] `clojure -M:test` pasa en el worktree de la story.
- [ ] Si se edita Markdown, `npx markdownlint-cli2 '**/*.md' '#node_modules'`
      pasa.
- [ ] La integración a `main` se hace de a una story, con checks verdes después
      de cada merge.

### #40 Lint no bloqueante

- [ ] Existe función de dominio pura para lint, separada de validación
      bloqueante.
- [ ] Bloque de código cercado sin lenguaje emite advertencia estable.
- [ ] Mensaje largo dentro del límite duro emite advertencia, no error.
- [ ] Mensaje por encima del límite duro sigue fallando con `:markdown/too-long`
      y no se persiste.
- [ ] HTML crudo, links inseguros e imágenes siguen bloqueando por la política de
      seguridad existente.
- [ ] `send-message!` persiste mensajes con advertencias sin cambiar el Markdown
      original ni el orden de mensajes.
- [ ] La respuesta de envío conserva compatibilidad razonable con consumidores
      actuales o documenta el cambio de contrato.
- [ ] Reintentos idempotentes con el mismo `client-txn-id` devuelven advertencias
      consistentes sin crear eventos nuevos.

### #41 Snapshot simple

- [ ] `create-room-snapshot!` devuelve `:snapshot/id`, `:snapshot/room-id`,
      `:snapshot/actor-id` y `:snapshot/last-sequence`.
- [ ] En sala con mensajes, `:snapshot/last-sequence` coincide con la última
      secuencia incluida.
- [ ] Sala vacía queda fijada por test; la implementación observada permite
      `:snapshot/last-sequence 0`.
- [ ] Crear snapshot no modifica mensajes ni eventos de mensaje.
- [ ] Actor inexistente, sala inexistente y actor sin acceso fallan con errores
      estructurados compatibles con el dominio.
- [ ] No hay commits Dolt reales ni dependencia de JDBC o filesystem.
- [ ] El identificador es estable y testeable; si se permite repetir snapshot de
      la misma sala/secuencia, la semántica queda clara.

## Estado observado de worktrees vecinos

### Estado #40

El worktree `story/40-lint-no-bloqueante` está en la misma base que `main` y
contiene cambios no commiteados solo en tests:

- `test/pi_clojure/domain/markdown_test.clj` agrega expectativas para
  `lint-message-markdown` y advertencias de mensaje largo.
- `test/pi_clojure/domain/user_test.clj` agrega expectativas de persistencia con
  advertencias y bloqueo por límite duro.

Evaluación: todavía no parece listo para checks finales porque las funciones y
constantes esperadas por los tests no están implementadas. Mantenerlo como ciclo
Red activo.

### Estado #41

El worktree `story/41-snapshot-simple-sala` contiene cambios no commiteados en:

- `src/pi_clojure/domain/user.clj`.
- `test/pi_clojure/domain/user_test.clj`.

Checks ejecutados en ese worktree:

- `clj-kondo --lint src test`: OK, 0 errores, 0 warnings.
- `clojure -M:test`: OK, 23 tests, 169 assertions, 0 failures, 0 errors.

Evaluación: funcionalmente cercano a integración, con observaciones de edge
cases antes de merge.

## Riesgos de conflicto

- #40 probablemente tocará `src/pi_clojure/domain/markdown.clj`,
  `src/pi_clojure/domain/user.clj` y quizás `src/pi_clojure/cli.clj`.
- #41 toca `src/pi_clojure/domain/user.clj`, archivo caliente compartido con #40
  para el camino de `send-message!`.
- Si #40 cambia el mapa de mensaje para incluir `:message/warnings`, puede
  afectar tests de lectura, exportación o snapshots que comparen mensajes
  completos.
- Si #41 agrega almacenamiento `:snapshots/by-id` al store, conviene integrarlo
  antes de cambios amplios en `create-store` o helpers de dominio.
- #40 depende conceptualmente de la política de seguridad Markdown ya integrada;
  no debería relajar validaciones bloqueantes.
- #41 reutiliza `require-export-access!`; si futuras stories renombran o
  generalizan autorización, puede requerir un refactor posterior para evitar
  acoplar snapshots a exportación.

## Orden de integración recomendado

1. Integrar #41 primero si se mantiene el cambio acotado actual y se commitea con
   checks verdes. Toca `user.clj`, pero no invade Markdown ni CLI.
2. Rebasar o actualizar #40 sobre `main` después de #41, porque #40 también puede
   tocar `user.clj` en `send-message!`.
3. Integrar #40 cuando tenga implementación completa, checks verdes y revisión de
   idempotencia.
4. Después de cada merge a `main`, correr `clj-kondo --lint src test` y
   `clojure -M:test` en `main` antes de cerrar issues.

## Edge cases sugeridos

### Edge cases #40

- Mensaje exactamente en el umbral de legibilidad.
- Mensaje exactamente en el límite duro y uno por encima.
- Bloque cercado con lenguaje (` ```clojure `) no debe advertir por falta de
  lenguaje.
- Múltiples bloques sin lenguaje: definir si se emite una o varias advertencias.
- Mensaje con advertencia de lint y link inseguro: debe bloquear por seguridad.
- Retry idempotente con `client-txn-id` compatible debe devolver la misma
  advertencia o recalcularla de forma pura sin duplicar eventos.
- Exportación de mensajes con `:message/warnings`: confirmar que no contamina el
  Markdown exportado.

### Edge cases #41

- Snapshot de sala compartida por participante activo vs usuario no participante.
- Snapshot de sala personal por dueño vs otro usuario.
- Actor inexistente y sala inexistente con errores estructurados.
- Repetir snapshot para misma sala y misma secuencia: confirmar si sobrescribe de
  forma idempotente o si debería registrar historial.
- Crear snapshot después de nuevos mensajes: el nuevo id debe reflejar la nueva
  última secuencia.
- Confirmar que `:snapshots/by-id` no aparece en APIs públicas no relacionadas.
