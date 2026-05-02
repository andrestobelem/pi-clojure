# Discovery del próximo lote después de #50/#51

Fecha: 2026-05-02.

Rol: discovery/reviewer del ciclo auto-mejorable. Alcance en modo lectura para
las issues #50/#51; no se implementó código ni se editaron `src/` o
`test/`.

## Estado observado

- Worktree actual: `review/discovery-next-y-ciclo`, limpio al iniciar.
- Issues abiertas al revisar: #50 y #51.
- #50 está `In Progress`, `Foco: Ahora`, `Canvas: Participación`.
- #51 está `In Progress`, `Foco: Ahora`, `Canvas: Producto`.
- No había otras issues abiertas antes de este discovery.
- El Project respeta el máximo de dos streams activos: #50 y #51.

## Observación de #50

Worktree: `../pi-clojure-50-verificar-acceso-no-participantes`.

Se observó un cambio local no commiteado en `test/pi_clojure/cli_test.clj`. El
diff agrega un test de aceptación para `send`, `show` y `export` con `mallory`
como usuaria existente no unida a la sala, verificando exit code no cero, stdout
vacío y ausencia de filtrado del mensaje sensible o participantes.

Señal de ciclo: hay Red/characterization en progreso. No se revisó ni modificó
implementación.

## Observación de #51

Worktree: `../pi-clojure-51-checklist-ciclo-multiagente`.

Se observó un documento nuevo no commiteado:
`docs/multi-agent-cycle-checklist.md`. Cubre intake, transición a
`In Progress`, Red/Green/Refactor/Commit, checks locales, paralelización,
integración, cierre y señal mínima por estado.

Señal de ciclo: hay slice documental enfocado que cubre la fricción detectada en
`docs/ideas/evaluation-48-50-and-cycle.md`.

## Lectura de backlog y duplicados

`docs/ideas/next-backlog.md` quedó mayormente integrado en issues cerradas:
las issues #30-#41 cubren errores CLI, export a archivo, participantes activos,
idempotencia incompatible, demo, validación Markdown, auditoría,
metadata/export, acceso de exportación, política de links/imágenes, lint y
snapshots.

Las siguientes propuestas no se recrearon porque ya están cerradas o absorbidas:

- descubrimiento/listado de salas: #48;
- validación estructural de mensajes dogfood/backlog: #49;
- errores seguros para no participantes: #50;
- checklist operativo humano del ciclo: #51.

## Próximo lote recomendado

### 1. Terminar #50 y #51 sin abrir más `In Progress`

Mantener #50 y #51 como los dos streams activos hasta integrarlos o pausar uno.
No conviene mover el nuevo lote a `In Progress` antes de liberar foco.

### 2. #52 Escrituras de estado seguras ante concurrencia

Issue creada: #52.

Motivo: `docs/demo-agent-roundtable.md` declara que el estado EDN de la CLI no
tiene locking y que la demo serializa turnos. Cuando el ciclo multiagente pase
de turnos serializados a agentes simultáneos, el riesgo más claro es pérdida de
mensajes o corrupción de estado.

Corte recomendado: proteger el store EDN local usado por la CLI, sin introducir
Dolt ni rediseñar persistencia.

Project sugerido/aplicado:

- `Status: Todo`;
- `Foco: Siguiente`;
- `Canvas: Producto`.

### 3. #53 Auditoría ejecutable de issues, worktrees y Project

Issue creada: #53.

Motivo: #51 deja un checklist humano. El siguiente incremento no duplicado es
convertir reglas mecánicas en señal ejecutable: cantidad de `In Progress`,
`Foco: Ahora`, worktrees faltantes, cambios pendientes y conflictos.

Corte recomendado: script o comando de solo lectura que reporte inconsistencias,
sin modificar Project ni cerrar issues automáticamente.

Project sugerido/aplicado:

- `Status: Todo`;
- `Foco: Después`;
- `Canvas: Producto`.

## Orden sugerido después de integrar #50/#51

1. Ejecutar #52 si el equipo quiere dogfood con agentes realmente paralelos.
2. Ejecutar #53 si vuelve a aparecer fricción de coordinación o estados
   ambiguos entre Project y worktrees.
3. No abrir nuevos streams si #50/#51 siguen `In Progress`; respetar el límite de
   dos implementaciones activas.

## Checks

- Se editó solo Markdown en este worktree.
- Pendiente al escribir este documento: `npx markdownlint-cli2 '**/*.md'
  '#node_modules'`.
