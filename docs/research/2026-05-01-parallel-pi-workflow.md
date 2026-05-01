# Trabajo paralelo con múltiples pi

## Contexto

Queremos aumentar throughput sin perder trunk-based development, TDD ni commits
atómicos. La necesidad concreta es tener dos instancias de pi implementando
stories distintas y una tercera instancia revisando y mejorando stories del
backlog.

Pi no incluye sub-agents como abstracción principal. La forma simple y explícita
de paralelizar es correr varias instancias pi, cada una en su propio `git
worktree`.

## Decisión

Usar tres worktrees:

- `pi-clojure-a`: implementación de una story.
- `pi-clojure-b`: implementación de otra story independiente.
- `pi-clojure-review`: revisión/refinamiento de issues, Project y backlog.

Cada worktree debe tener una branch distinta cuando modifica el repo. El reviewer
puede trabajar solo en GitHub Issues/Project sin tocar archivos; si modifica
documentación o prompts, debe crear su propia branch.

## Reglas de coordinación

- Una story activa por worktree/pi.
- Máximo dos streams de implementación en paralelo.
- Un stream adicional puede revisar/refinar backlog.
- Evitar elegir stories que toquen intensivamente los mismos archivos.
- Antes de empezar, cambiar contexto o cerrar una story, correr
  `git status --short --branch`.
- No avanzar si hay conflictos (`UU`) o cambios pendientes no clasificados.
- Integrar a `main` frecuentemente y solo con fast-forward.
- Después de integrar, borrar la branch local y remota.

## Comandos base

Crear worktrees:

```sh
cd /Users/andrestobelem/ws/at/pi-clojure

git worktree add ../pi-clojure-a main
git worktree add ../pi-clojure-b main
git worktree add ../pi-clojure-review main
```

Iniciar pi en cada worktree:

```sh
cd /Users/andrestobelem/ws/at/pi-clojure-a
pi
```

```sh
cd /Users/andrestobelem/ws/at/pi-clojure-b
pi
```

```sh
cd /Users/andrestobelem/ws/at/pi-clojure-review
pi
```

## Selección de stories

Para los dos pis de implementación, preferir stories de bajo solapamiento:

- una story de dominio y otra de documentación/CLI;
- una story de mensajes y otra de exportación, si no tocan los mismos módulos;
- evitar dos refactors simultáneos sobre el mismo namespace.

El pi reviewer debe buscar:

- duplicados;
- criterios de aceptación ambiguos;
- stories demasiado grandes;
- stories parcialmente cubiertas por trabajo reciente;
- issues técnicos que conviene convertir en refactor o absorber.

## Riesgos y mitigaciones

| Riesgo | Mitigación |
| --- | --- |
| Conflictos frecuentes | Elegir stories con bajo solapamiento y hacer commits pequeños. |
| Dos pis cambian la misma story | Una story activa por worktree/pi y Project actualizado. |
| Reviewer modifica código activo | Reviewer solo edita issues/Project, salvo branch propia coordinada. |
| Branches largas | Integrar frecuentemente a `main` con fast-forward. |
| Estado local confuso | Revisar `git status --short --branch` al inicio y al cierre. |

## Prompt operativo sugerido

Usar `.pi/prompts/parallel-work.md` para preparar el reparto de trabajo y los
mensajes iniciales para cada instancia pi.
