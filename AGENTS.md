# Agent instructions

Responder siempre en español, salvo que el usuario pida explícitamente otro idioma.

Este repo usa:

- Test Driven Development / Test Driven Design
- Trunk-based development
- Conventional Commits
- Commits atómicos

## Desarrollo

Trabajar en ciclos cortos:

1. Red: escribir primero un test o ejemplo que falle.
2. Green: implementar lo mínimo para pasar.
3. Refactor: mejorar diseño con la suite en verde.
4. Commit: con los checks en verde, hacer un commit atómico.

Usar los tests como feedback de diseño, no solo como cobertura.

Trabajar de a una user story activa por worktree/pi en GitHub Projects. Al
iniciar, pausar o terminar una historia, actualizar su estado en el proyecto.

Se puede paralelizar con múltiples instancias de pi solo si cada una usa un
git worktree separado y una branch distinta. Mantener como máximo dos streams de
implementación en paralelo y un stream adicional de revisión/refinamiento de
backlog.

Antes de iniciar una historia o cambiar de contexto, revisar
`git status --short --branch`. No avanzar si hay conflictos (`UU`) o cambios
pendientes sin clasificar; primero resolverlos, commitearlos, stashearlos o
crear una tarea explícita.

En trabajo paralelo, evitar que dos pis editen intensivamente los mismos
archivos. El pi de revisión no debe modificar código de una story activa salvo
que cree su propia branch y coordine el cambio.

Al refinar backlog o stories, mantener actualizado el GitHub Project:

- usar `Status` para estado operativo (`Todo`, `In Progress`, `Done`);
- usar `Foco` para ordenar trabajo (`Ahora`, `Siguiente`, `Después`, `Pausado`);
- usar `Canvas` para ubicar la historia en el mapa del MVP;
- mantener como máximo 1-2 historias en `In Progress`;
- retirar del board issues duplicados, cerrados irrelevantes o tareas técnicas
  que ya estén absorbidas por una story de usuario.

Si una story ya quedó parcialmente cubierta por otra, no cerrarla sin revisión:
refinarla a una verificación explícita y acotada si aporta señal, o cerrarla /
absorberla si es duplicada.

No avanzar a otra funcionalidad si quedó pendiente la fase de refactor. Si el
diseño se vuelve confuso, se puede borrar `src/` y `test/` para reiniciar la
implementación, preservando documentación, decisiones, backlog y tooling.

## Git

- `main` es la rama principal de integración.
- Crear la branch corta por story desde GitHub con `gh issue develop`, basada
  en `main` y con formato `story/<issue-number>-<slug>`.
- Evitar ramas de larga vida.
- Integrar cambios pequeños y frecuentes.
- Hacer commits pequeños, atómicos y con una intención clara.
- Después de cada ciclo TDD con tests verdes, commitear el cambio atómico.
- Usar Conventional Commits.
- Al terminar una story, integrar rápido a `main`, pushear `main`, borrar la
  branch de la story local y remota, cerrar el issue con checks y dejar el
  working tree limpio.

## Documentación externa

Cuando necesitemos documentación actualizada de librerías o herramientas, usar
Context7 desde CLI si está disponible:

```sh
set -a
[ -f .env.local ] && . ./.env.local
set +a
npx ctx7 library <nombre> "<consulta>"
npx ctx7 docs <library-id> "<consulta>"
```

No mostrar ni commitear `CONTEXT7_API_KEY`.

## Exploración de tecnología e infraestructura

Cuando se evalúe una tecnología nueva de infraestructura, base de datos o
servicio local:

- Guardar la investigación o decisión en `docs/research/`.
- Documentar comandos reproducibles en `README.md` cuando sean de uso frecuente.
- Dejar configuración local no sensible en `.env.example`.
- Mantener secretos, API keys y credenciales reales fuera de git.
- Validar Docker Compose con:

```sh
docker compose config
```

## Clojure

Después de editar código Clojure, correr:

```sh
clj-kondo --lint src test
clojure -M:test
```

## Markdown

Después de editar Markdown, correr:

```sh
npx markdownlint-cli2 '**/*.md' '#node_modules'
```
