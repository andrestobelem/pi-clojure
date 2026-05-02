# Evaluación de stories #52/#53

Fecha: 2026-05-02.

Rol: evaluator/backlog gardener y guardián del ciclo para #52/#53. Alcance en
modo lectura sobre los worktrees de implementación; no se editó código ni se
modificaron `src/` o `test/`.

## Estado observado

- Worktree de evaluación: `review/evaluar-52-53`.
- Issues abiertas observadas: #52 y #53.
- #52 está `Status: In Progress`, `Foco: Ahora`, `Canvas: Producto`.
- #53 está `Status: In Progress`, `Foco: Ahora`, `Canvas: Producto`.
- Worktree #52: `../pi-clojure-52-lock-estado-cli`, branch
  `story/52-lock-estado-cli`, sin cambios locales y sin commits por delante de
  `main` al revisar.
- Worktree #53: `../pi-clojure-53-auditoria-ciclo-multiagente`, branch
  `story/53-auditoria-ciclo-multiagente`, sin cambios locales y sin commits por
  delante de `main` al revisar.
- El límite operativo se cumple: hay exactamente dos streams de implementación
  en `In Progress`/`Foco: Ahora`.

## Lectura de ciclo

`docs/multi-agent-cycle-checklist.md` define que una issue en `In Progress`
necesita worktree/branch asignado y primer Red identificado o iniciado. Las
historias #52 y #53 cumplen la parte de worktree/branch y tienen primer test
rojo sugerido en el
issue, pero las historias todavía no muestran señal local de Red en los
worktrees observados.

La recomendación de guardia es sostener el foco actual y no abrir más streams
hasta que al menos una de estas historias tenga un ciclo Red/Green/Refactor con
commit atómico, o sea pausada explícitamente con Project actualizado.

## Slice recomendado para #52

Objetivo: proteger las escrituras del store EDN local sin rediseñar la
persistencia ni introducir Dolt.

### Corte 1: caracterización de concurrencia

- Primer Red: dos procesos o futures ejecutan `chat send` contra el mismo
  `PI_CHAT_STATE_FILE`, con usuarios participantes y `client-txn-id` distintos.
- Señal esperada: al finalizar, el EDN parsea y contiene ambos mensajes
  exactamente una vez, o al menos un comando falla de forma segura sin escritura
  parcial.
- Mantener el test cerca del flujo CLI porque el riesgo está en read-modify-write
  del archivo, no solo en el dominio puro.

### Corte 2: primitive mínima de escritura segura

- Introducir un único camino de escritura serializada para el archivo de estado.
- Usarlo para `create-user`, `create-room`, `join`, `leave` y `send`.
- Evitar cambios de comportamiento en comandos de solo lectura: `show`, `export`
  y `rooms` no deben escribir estado.

### Corte 3: UX de falla y documentación dogfood

- Si el lock no puede obtenerse, devolver exit code no cero y un error
  accionable.
- Documentar en la demo si la ejecución paralela queda soportada directamente o
  si los agentes deben usar retry/backoff.

## Riesgos de #52

- Tests flaky si se basan en timing. Preferir sincronización explícita o un
  mecanismo testeable de lock ocupado.
- Bloqueos indefinidos si el lock espera sin timeout. Definir comportamiento
  acotado y observable.
- Divergencia entre comandos si algunos escriben fuera del camino seguro.
- Mezclar locking con rediseño de persistencia. Dolt, snapshots y cambios de
  storage deberían quedar fuera de este slice.
- Conflicto de integración con #53 si ambos editan README o docs de ciclo. La
  zona de código esperada de #52 es CLI/store; coordinar solo documentación
  compartida.

## Checks para #52 antes de integrar

- `clj-kondo --lint src test`.
- `clojure -M:test`.
- `npx markdownlint-cli2 '**/*.md' '#node_modules'` si se edita documentación.
- `git diff main...HEAD` enfocado en locking y documentación asociada.
- Verificar `git status --short --branch` limpio después del commit.

## Slice recomendado para #53

Objetivo: convertir reglas mecánicas del checklist humano en una auditoría
local, de solo lectura y sin efectos laterales sobre GitHub Project.

### Corte 1: salida estable con fixture o modo seco

- Primer Red: una fixture con tres items `In Progress`, un item sin worktree y un
  worktree con cambios pendientes.
- La auditoría debe reportar advertencias estables y exit code no cero, sin
  tocar GitHub ni archivos de código.
- Diseñar primero el formato de salida para que sea útil como guardia antes de
  iniciar, pausar o cerrar historias.

### Corte 2: adaptadores de datos reales

- Listar issues abiertas con `Status`, `Foco`, branch/worktree local detectable
  y estado git resumido.
- Separar parsing/decisión de los comandos externos para que la lógica sea
  testeable sin depender siempre de red o credenciales.

### Corte 3: documentación de uso

- Documentar cuándo correr el script: antes de pasar una issue a `In Progress`,
  al pausar una story, antes de integración y durante revisión de backlog.
- Declarar explícitamente que no modifica Project, no cierra issues y no limpia
  worktrees.

## Riesgos de #53

- Acoplar la prueba a GitHub real y volverla lenta o frágil. El núcleo debería
  poder validarse con datos simulados.
- Incluir autocorrecciones. La story pide auditoría, no bot de mantenimiento.
- Detectar worktrees por convenciones demasiado rígidas. Reportar incertidumbre
  es mejor que fallar silenciosamente.
- Exponer secretos o dumps grandes de Project. La salida debe ser mínima y
  accionable.
- Duplicar #51. #53 debe producir señal ejecutable; si solo agrega otra lista
  documental, no aporta incremento nuevo.

## Checks para #53 antes de integrar

- Checks propios del script o comando agregado.
- `clj-kondo --lint src test` y `clojure -M:test` si el script toca Clojure o
  tests Clojure.
- `npx markdownlint-cli2 '**/*.md' '#node_modules'` si se edita documentación.
- Verificar manualmente que el script no modifica Project ni cierra issues.
- `git diff main...HEAD` enfocado en auditoría y documentación de uso.
- Verificar `git status --short --branch` limpio después del commit.

## Orden de integración recomendado

1. Integrar #52 primero si ya tiene Red/Green/Refactor verde. Reduce el riesgo
   operativo para dogfood con agentes paralelos y puede cambiar la
   documentación de la demo.
2. Integrar #53 después, rebasado sobre `main` actualizado. Así la auditoría
   puede reconocer el estado final de #52 y documentar reglas vigentes.
3. Si #53 avanza antes que #52, se puede integrar primero solo si no toca las
   mismas secciones documentales y mantiene su alcance estrictamente de solo
   lectura.
4. No mover nuevas issues a `In Progress` mientras #52 y #53 sigan activas con
   `Foco: Ahora`.

## Necesidad de nueva story después de esta tanda

No conviene crear una nueva story ahora. El ciclo ya está en el máximo de dos
streams activos y las dos historias actuales cubren los riesgos inmediatos no
duplicados: seguridad de escrituras concurrentes (#52) y señal ejecutable del
ciclo (#53).

Después de integrar ambas, la próxima decisión debería basarse en evidencia de
uso:

- si #52 deja solo falla segura y aparece fricción real de agentes simultáneos,
  considerar una story acotada de retry/backoff para comandos de escritura;
- si #53 detecta inconsistencias repetidas que requieren acción manual, evaluar
  una story posterior de autocorrección asistida, manteniendo aprobación humana;
- si no aparece nueva fricción, volver al roadmap de producto en vez de crear
  trabajo de proceso adicional.

## Decisión de backlog

- No se crearon issues nuevas.
- No se cerraron issues.
- Mantener #52 y #53 como las únicas stories activas hasta integrar o pausar una
  de ellas.
