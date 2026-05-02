# Checklist operativo del ciclo multiagente

Este checklist convierte transcripts dogfood, issues, worktrees y Project en un
ciclo repetible. Usarlo al crear, iniciar, pausar, integrar y cerrar historias.

## Reglas base

- Una story activa por worktree/pi.
- Máximo dos streams de implementación en `In Progress`.
- `Foco: Ahora` solo para streams con trabajo activo en un worktree/branch.
- El stream de revisión puede refinar backlog, pero no debe modificar código de
  una story activa sin branch propia y coordinación explícita.
- No avanzar con conflictos `UU` ni cambios locales sin clasificar.
- No marcar `Done` sin checks verdes, commit integrado y working tree limpio.

## 1. Intake desde transcripts dogfood

Usar cuando una conversación dogfood propone trabajo nuevo.

- [ ] Identificar el transcript y el fragmento que justifica la historia.
- [ ] Formular la historia como usuario, necesidad y resultado esperado.
- [ ] Definir criterios de aceptación verificables y acotados.
- [ ] Nombrar el primer test rojo o ejemplo que probaría el comportamiento.
- [ ] Detectar duplicados o cobertura parcial por historias existentes.
- [ ] Si es duplicada, cerrar/absorber solo después de revisión explícita.
- [ ] Si aporta señal, crear o refinar issue con labels, milestone y Project.
- [ ] Ubicar `Canvas`, `Foco` y `Status` sin saturar `In Progress`.

## 2. Antes de pasar una issue a `In Progress`

Una issue no pasa a `In Progress` solo por parecer prioritaria. Debe tener una
acción explícita de arranque.

- [ ] `git status --short --branch` está limpio en el worktree elegido.
- [ ] No hay conflictos `UU` ni cambios pendientes sin decidir.
- [ ] Existe branch corta vinculada al issue, basada en `main`.
- [ ] Existe worktree dedicado si se trabaja en paralelo.
- [ ] El primer test rojo está identificado y es ejecutable o verificable.
- [ ] Hay como máximo otro stream de implementación en `In Progress`.
- [ ] `Status` queda en `In Progress` solo si el stream empieza ahora.
- [ ] `Foco` queda en `Ahora` solo para streams activos.
- [ ] Si no hay capacidad, dejar `Status: Todo` y usar `Foco: Siguiente`,
  `Después` o `Pausado` según corresponda.

Si falta branch/worktree, primer test rojo o coherencia de Project, registrar la
acción pendiente y no mover la issue a `In Progress`.

## 3. Ciclo TDD/TDD Design por story

### Red

- [ ] Escribir primero un test, ejemplo o verificación documental que falle.
- [ ] Para documentación, puede ser comprobar que falta el documento, enlace o
  sección esperada, más `markdownlint` si aplica.
- [ ] Confirmar que la falla describe el comportamiento deseado.
- [ ] Evitar ampliar alcance por encima de la historia activa.

### Green

- [ ] Implementar el mínimo cambio que hace pasar el test o verificación.
- [ ] Mantener el diff enfocado en la historia.
- [ ] Evitar tocar archivos editados intensivamente por otro worktree activo.

### Refactor

- [ ] Mejorar nombres, estructura o duplicación con la suite en verde.
- [ ] No saltar a otra funcionalidad si quedó refactor pendiente.
- [ ] Si el diseño se volvió confuso, reiniciar código preservando docs,
  decisiones, backlog y tooling.

### Commit

- [ ] Correr checks locales obligatorios para los archivos editados.
- [ ] Revisar `git diff` y confirmar que no mezcla otra story.
- [ ] Hacer un commit atómico con Conventional Commits.
- [ ] Mantener commits pequeños y frecuentes.

## 4. Checks locales

- [ ] Si se editó Clojure: `clj-kondo --lint src test`.
- [ ] Si se editó Clojure: `clojure -M:test`.
- [ ] Si se editó Markdown:
  `npx markdownlint-cli2 '**/*.md' '#node_modules'`.
- [ ] Si se editó Docker Compose: `docker compose config`.
- [ ] Si se incorporó investigación externa, documentarla en `docs/research/`.
- [ ] Confirmar que secretos o credenciales reales no quedaron en git.

## 5. Implementación paralela

- [ ] Mantener cada pi en su worktree y branch.
- [ ] Coordinar si dos historias necesitan tocar los mismos archivos centrales.
- [ ] Pausar o devolver a `Todo` un stream sin señal local de Red.
- [ ] Cuando se pausa, dejar nota de estado, próximos pasos y checks pendientes.
- [ ] Usar `Foco: Pausado` si el stream conserva contexto pero no capacidad.
- [ ] Usar `Foco: Siguiente` o `Después` para backlog sin ejecución activa.

## 6. Evaluación e integración

Antes de pedir integración o fusionar a `main`:

- [ ] La historia tiene commits atómicos y enfocados.
- [ ] Los checks locales relevantes están verdes.
- [ ] El diff contra `main` no mezcla otra historia.
- [ ] La documentación de usuario está actualizada si cambió el flujo visible.
- [ ] El Project refleja el estado real del stream.

Al integrar:

- [ ] Actualizar desde `main` con rebase/merge corto y sin cambios ajenos.
- [ ] Repetir checks si hubo rebase, merge o resolución de conflictos.
- [ ] Integrar el commit a `main` y pushear `main`.
- [ ] Verificar que el worktree de la story queda limpio.

## 7. Marcado de checklists, cierre y limpieza

No marcar checklists del issue ni cerrar por intención; hacerlo solo con
evidencia verificable.

- [ ] Marcar cada criterio de aceptación solo después de verificarlo.
- [ ] Dejar comentario final con resumen, commit(s), checks y estado de git.
- [ ] Mover `Status: Done` solo si el cambio está integrado en `main`.
- [ ] Confirmar checks verdes después de la integración cuando aplique.
- [ ] Confirmar `git status --short --branch` limpio.
- [ ] Cerrar la issue después de integrar, actualizar Project y limpiar estado.
- [ ] Borrar branch local/remota de la story cuando ya no sea necesaria.
- [ ] Eliminar o archivar worktrees temporales que quedaron sin uso.

## Señal mínima para estados

| Estado operativo | Señal requerida |
| --- | --- |
| `Todo` | Issue refinada o por refinar, sin ejecución activa. |
| `In Progress` | Worktree/branch asignado y primer Red identificado o iniciado. |
| `Pausado` | Contexto preservado, sin capacidad activa o bloqueada. |
| `Done` | Checks verdes, commit integrado, Project actualizado y árbol limpio. |

## Verificación rápida de coherencia

Si una issue candidata no tiene branch/worktree, primer test rojo identificado y
estado de Project coherente, no puede pasar a `In Progress` sin una acción
explícita: crear branch/worktree, definir Red o corregir Project.
