# Patrones de agentes para backlog e implementación

Fecha: 2026-05-01

## Contexto

Queremos usar varios agentes para mantener vivo el backlog y programar
funcionalidad con TDD, trunk-based development y commits atómicos. El patrón de
trabajo del repo ya funciona bien con worktrees separados, GitHub Issues y un
Project como tablero operativo.

La investigación externa de 2026 coincide en una idea central: los agentes son
capacidad paralela supervisada. Funcionan mejor cuando tienen tareas pequeñas,
criterios verificables, aislamiento por branch/worktree y revisión humana o de
otro agente antes de integrar.

## Fuentes revisadas

- Anthropic, `2026 Agentic Coding Trends Report`: describe el cambio de escribir
  código a orquestar agentes, con foco en coordinación multi-agente, calidad y
  supervisión humana.
- OpenAI, `Introducing the Codex app`: presenta la gestión de múltiples agentes
  en paralelo con worktrees aislados, revisión de diffs y tareas largas.
- GitHub Docs, `About GitHub Copilot cloud agent`: documenta el flujo desde issue
  hacia plan, branch, cambios, tests/linters y PR.
- Anthropic Docs, `Best Practices for Claude Code`: recomienda separar
  exploración, plan, implementación y commit; usar criterios verificables y
  subagentes para investigación/revisión.
- Anthropic Engineering, `Harness design for long-running application
  development`: describe arquitecturas planner-generator-evaluator, contratos de
  sprint y evaluación automatizada.

## Patrones útiles para este repo

### 1. Tríada Discovery -> Refinement -> Implementation

Usar tres tipos de stream:

- Discovery: imagina oportunidades, revisa visión, docs e issues cerradas. No
  toca `src` ni `test`.
- Refinement: convierte ideas en issues pequeñas con criterios de aceptación,
  dependencias, riesgos y slicing.
- Implementation: toma una issue refinada y la implementa con TDD en worktree
  aislado.

Regla: una historia no entra a implementación si no tiene criterios verificables
y comandos de checks explícitos.

### 2. Planner / Generator / Evaluator

Para stories medianas, separar roles:

- Planner: lee issue y repo, propone plan y primer test rojo.
- Generator: implementa el mínimo para pasar y refactoriza.
- Evaluator: corre checks, revisa diff, busca edge cases y pide ajustes.

En nuestro flujo, el planner puede ser el mismo agente al inicio de la sesión,
pero conviene lanzar un evaluator separado para stories sensibles como permisos,
idempotencia o exportación.

### 3. Dos implementadores + un reviewer

Mantener como patrón operativo por defecto:

- Stream A: implementación de una story.
- Stream B: implementación de otra story con bajo solapamiento de archivos.
- Stream Review: refinamiento, QA, documentación o revisión de diffs.

Evitar tres implementadores simultáneos salvo que las áreas sean claramente
independientes.

### 4. Matriz de conflictos por archivo caliente

Antes de lanzar paralelo, clasificar stories por archivos probables:

- `src/pi_clojure/domain/user.clj`: dominio, participación, eventos,
  idempotencia.
- `src/pi_clojure/domain/markdown.clj`: validación y linting Markdown.
- `src/pi_clojure/cli.clj`: UX CLI, comandos, errores y persistencia EDN.
- `docs/ideas` y `docs/research`: discovery/refinement.

No lanzar dos implementadores sobre el mismo archivo caliente si ambos requieren
refactor profundo.

### 5. Contrato de story verificable

Cada issue debería tener:

- Historia de usuario en una frase.
- Criterios de aceptación en bullets testeables.
- Comandos obligatorios de verificación.
- Fuera de alcance explícito.
- Riesgo de conflicto con otras stories.
- Sugerencia de primer test rojo.

Esto reduce ambigüedad y acelera TDD.

### 6. Evaluator antes de merge

Antes de integrar una branch:

- Reejecutar checks en el worktree de la story.
- Revisar `git diff --stat` y que no haya cambios fuera de alcance.
- Rebase sobre `main` si otra story ya entró.
- Reejecutar checks finales en `main` después de merge.
- Recién entonces cerrar issue y borrar branch/worktree.

### 7. Backlog gardener continuo

Mantener un agente de jardinería de backlog cuando hay capacidad libre:

- Detecta duplicados o historias ya absorbidas.
- Divide issues grandes.
- Actualiza `Foco`, `Canvas` y `Status`.
- Documenta decisiones en `docs/ideas` o `docs/research`.
- No toca código de stories activas.

### 8. Spike acotado para incertidumbre técnica

Cuando una story depende de una librería o infraestructura nueva:

- Crear issue `type:research` o `type:idea`.
- Guardar investigación en `docs/research/`.
- No mezclar spike con implementación productiva.
- Transformar hallazgos en stories pequeñas.

## Flujo recomendado

1. Discovery semanal o por bloque:
   - 2-3 agentes proponen oportunidades desde producto, dominio y UX.
   - Un humano o agente coordinador sintetiza en `docs/ideas/next-backlog.md`.
2. Refinement:
   - Crear o actualizar issues con criterios verificables.
   - Marcar `Foco` y `Canvas`.
3. Implementación paralela:
   - Dos stories en worktrees separados.
   - Un stream reviewer/refinement.
4. Integración:
   - Checks por worktree.
   - Rebase/fast-forward.
   - Checks en `main`.
   - Cerrar issues y limpiar branches/worktrees.
5. Retrospectiva corta:
   - Documentar aprendizajes si cambian reglas de dominio o de proceso.

## Prompts base sugeridos

### Agente implementador

```text
Implementá la issue #<n> con TDD/TDD Design en este worktree.
Antes de editar, revisá git status y gh issue view.
Ciclo Red/Green/Refactor estricto.
Corré clj-kondo --lint src test y clojure -M:test.
Si editás Markdown, corré markdownlint.
Hacé commits atómicos con Conventional Commits.
No cierres la issue sin checks finales.
```

### Agente reviewer/evaluator

```text
Revisá la branch de la issue #<n> sin implementar nuevas features.
Verificá alcance, tests, edge cases, errores y documentación.
Corré checks.
Proponé cambios concretos o aprobá integración.
No cierres issues ni edites main.
```

### Agente backlog gardener

```text
Revisá issues, Project y docs de ideas.
Detectá duplicados, historias demasiado grandes y dependencias.
Propone slicing con criterios de aceptación testeables.
No edites src ni test.
Documentá la síntesis en docs/ideas/.
```

## Métricas livianas

- Tiempo desde issue `In Progress` hasta merge.
- Número de rebase/conflictos por tanda paralela.
- Porcentaje de stories con primer test rojo claro.
- Checks fallidos por causa: lint, test, alcance, integración.
- Issues cerradas que luego requieren fix inmediato.

## Recomendación para pi-clojure

Adoptar como default:

- 2 implementadores + 1 reviewer/backlog gardener.
- Discovery multi-agente solo cuando el backlog baje de 3-5 stories listas.
- Toda story nueva debe incluir primer test rojo sugerido.
- Todo merge debe pasar por evaluator/checks finales en `main`.
