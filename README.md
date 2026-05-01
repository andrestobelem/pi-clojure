# pi-clojure

pi clojure

## Visión

Queremos construir una aplicación de salas de chat **Markdown-first**, donde las
conversaciones no sean solo mensajes efímeros sino también documentos vivos,
versionables y exportables: cada sala permite conversar en tiempo real usando
Markdown seguro, conservar historial auditable de mensajes y ediciones,
recuperar y exportar la conversación como documento Markdown, y explorar un
modelo de persistencia tipo Git —empezando con Dolt— para habilitar diffs,
commits, snapshots y eventualmente ramas o forks de conversaciones que puedan
transformarse en conocimiento durable.

## Desarrollo

Este repo usa **Test Driven Development / Test Driven Design** y
**trunk-based development**.

### TDD / Test Driven Design

Regla de trabajo:

```text
Red -> Green -> Refactor -> Commit
```

Buenas prácticas:

- Escribir primero un test o ejemplo del comportamiento esperado.
- Implementar lo mínimo para que el test pase.
- Refactorizar con la suite en verde.
- Con tests y checks verdes, hacer un commit atómico.
- Usar los tests como feedback de diseño, no solo como cobertura.
- Preferir ciclos cortos y tests rápidos.

Correr linter y tests:

```sh
clj-kondo --lint src test
clojure -M:test
```

Al inicio de un reinicio TDD puede ser válido que `clojure -M:test` ejecute 0
tests, siempre que la próxima sesión empiece escribiendo un test rojo para la
historia activa.

### Trunk-based development

`main` es la rama principal de integración.

Buenas prácticas:

- Integrar cambios pequeños en `main` frecuentemente.
- Para cada story, crear la branch desde GitHub para que quede vinculada al
  issue:

  ```sh
  git switch main
  git pull --ff-only
  gh issue develop <issue-number> \
    --name story/<issue-number>-<slug> \
    --base main \
    --checkout
  ```

- Ejemplo:

  ```sh
  gh issue develop 21 \
    --name story/21-personal-room \
    --base main \
    --checkout
  ```

- Evitar ramas de larga vida.
- Si usamos ramas, que sean cortas y vuelvan rápido a `main`.
- Mantener `main` en estado consistente.
- Al terminar una story, integrar rápido a `main` y borrar la branch.
- Usar feature flags o branch by abstraction para cambios grandes.

## Gestión de proyecto

Usamos GitHub Issues para tareas concretas y milestones para agrupar objetivos.
Los milestones iniciales son:

- `MVP-0: núcleo del dominio`.
- `MVP-1: chat Markdown exportable`.

Labels principales:

- `type:*` para tipo de trabajo.
- `area:*` para área del producto o código.
- `priority:*` para prioridad relativa.

Historia activa:

- Ninguna.

Procedimiento de trabajo:

- [`docs/research/2026-05-01-pairing-tdd-reset.md`](docs/research/2026-05-01-pairing-tdd-reset.md)

Tablero GitHub Projects:

- [`pi-clojure MVP`](https://github.com/users/andrestobelem/projects/2)

Ver items activos:

```sh
gh project item-list 2 --owner andrestobelem --format json --limit 20 \
  --jq '.items[] | "#\(.content.number) \(.status) \(.content.title)"'
```

Mover una historia a `Todo`, `In Progress` o `Done` requiere el id del proyecto,
el id del item, el campo `Status` y el option id correspondiente. Consultarlos
con:

```sh
gh project view 2 --owner andrestobelem --format json --jq '.id'
gh project field-list 2 --owner andrestobelem --format json
gh project item-list 2 --owner andrestobelem --format json --limit 100 \
  --jq '.items[] | select(.content.number==23) | {id, status:.status, title:.content.title}'
```

Mover un item a `Done`:

```sh
gh project item-edit \
  --project-id <project-id> \
  --id <item-id> \
  --field-id <status-field-id> \
  --single-select-option-id <done-option-id>
```

Cerrar el issue con comentario de cierre:

```sh
gh issue close 20 --comment "Implementado en <commit-hash>.\n\nChecks:\n- clj-kondo --lint src test\n- clojure -M:test"
```

## Dolt

Este repo incluye una prueba local de Dolt para versionar datos de salas de chat
Markdown.

Levantar Dolt:

```sh
docker compose up -d dolt
```

Validar la configuración:

```sh
docker compose config
```

Aplicar el esquema inicial:

```sh
docker compose exec -T dolt \
  dolt --data-dir /var/lib/dolt/chat_markdown sql < db/dolt/001_chat_markdown.sql
```

Ver estado del servicio:

```sh
docker compose ps
docker compose logs --tail=80 dolt
```

Ver estado y log de Dolt:

```sh
docker compose exec dolt sh -lc \
  'cd /var/lib/dolt/chat_markdown && dolt status'

docker compose exec dolt sh -lc \
  'cd /var/lib/dolt/chat_markdown && dolt log'
```

Conectarse con un cliente MySQL compatible:

```sh
mysql \
  --host 127.0.0.1 \
  --port 3307 \
  --user chat \
  --password=chatpass \
  chat_markdown
```

Detener el servicio:

```sh
docker compose down
```

Más detalles: [`docs/research/2026-05-01-dolt.md`](docs/research/2026-05-01-dolt.md).

## Commits

Este repo usa [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/).

Formato:

```text
<type>[optional scope][!]: <description>
```

Ejemplos:

```text
feat(parser): support nested expressions
fix: handle nil inputs
docs!: rewrite public API guide
```

Tipos permitidos por el hook local: `build`, `chore`, `ci`, `docs`, `feat`, `fix`, `perf`, `refactor`, `revert`, `style`, `test`.

### Commits atómicos

Además, en este repo usamos commits atómicos: cada commit debe representar un único cambio lógico.

Buenas prácticas:

- No mezclar features, fixes, refactors, documentación o formateo no relacionados.
- Incluir tests y docs en el mismo commit solo si pertenecen al mismo cambio lógico.
- Mantener el repo en estado consistente después de cada commit, siempre que sea posible.
- Usar `git add -p` para separar cambios mezclados.
- Si cuesta elegir un solo `type`/`scope` de Conventional Commits, probablemente conviene dividir el commit.

Para activar la validación y el template en una copia nueva del repo:

```sh
git config core.hooksPath .githooks
git config commit.template .gitmessage
```
