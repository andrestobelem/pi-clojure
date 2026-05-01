# Conventional Commits

Fecha: 2026-05-01

## Resumen

Conventional Commits es una convención liviana para escribir mensajes de commit con una estructura predecible. El objetivo es que el historial sea más fácil de leer y que herramientas automáticas puedan generar changelogs, calcular versiones SemVer y validar commits.

Formato general:

```text
<type>[optional scope][!]: <description>

[optional body]

[optional footer(s)]
```

Ejemplos:

```text
feat(auth): add OAuth2 login
fix(api): return 404 for missing entries
docs: update installation guide
refactor(core): simplify parser
feat(api)!: remove legacy v1 endpoint
```

## Relación con SemVer

La especificación Conventional Commits 1.0.0 define esta relación con Semantic Versioning:

- `fix:` corresponde a un cambio de tipo **PATCH**.
- `feat:` corresponde a un cambio de tipo **MINOR**.
- Un breaking change corresponde a un cambio de tipo **MAJOR**.

Un breaking change puede indicarse de dos maneras:

```text
feat(api)!: remove legacy v1 endpoint
```

o usando un footer:

```text
feat(api): remove legacy v1 endpoint

BREAKING CHANGE: v1 endpoints are no longer supported.
```

También se acepta `BREAKING-CHANGE` como sinónimo en footers.

## Tipos comunes

La especificación solo asigna significado SemVer especial a `feat`, `fix` y breaking changes. Sin embargo, en la práctica muchas herramientas usan una lista convencional de tipos:

- `feat`: nueva funcionalidad
- `fix`: corrección de bug
- `docs`: cambios en documentación
- `style`: formato, espacios, lint, sin cambios funcionales
- `refactor`: refactor sin cambiar comportamiento observable
- `test`: agregar o modificar tests
- `chore`: tareas de mantenimiento
- `build`: cambios en build, dependencias o tooling de build
- `ci`: cambios en integración continua
- `perf`: mejoras de performance
- `revert`: revertir un commit anterior

## Scope

El scope es opcional y sirve para indicar el área afectada:

```text
feat(parser): support nested expressions
fix(auth): handle expired token refresh
```

Conviene que los scopes sean cortos, estables y relacionados con módulos, paquetes o áreas del producto.

## Body y footers

El encabezado debe ser breve. Si hace falta explicar contexto, motivación o detalles técnicos, se puede usar un body separado por una línea en blanco:

```text
fix(worker): avoid duplicate job execution

The lock renewal could race with job completion when the worker was under high load.
```

Los footers sirven para metadatos, referencias a issues o breaking changes:

```text
Refs: #123
BREAKING CHANGE: config files must now use YAML.
```

## Beneficios

- Historial de commits más consistente.
- Changelogs generados automáticamente.
- Versionado automático siguiendo SemVer.
- Mejor integración con herramientas como commitlint, semantic-release, release-please y conventional-changelog.
- Facilita revisar cambios por tipo: features, fixes, docs, refactors, etc.

## Herramientas relacionadas

- **commitlint**: valida que los mensajes sigan una configuración determinada.
- **@commitlint/config-conventional**: preset común con tipos como `build`, `chore`, `ci`, `docs`, `feat`, `fix`, `perf`, `refactor`, `revert`, `style` y `test`.
- **conventional-changelog**: genera changelogs a partir del historial de commits.
- **semantic-release**: automatiza releases en CI/CD: analiza commits, calcula la próxima versión, genera release notes, crea tags y publica paquetes.
- **release-please**: alternativa moderna para automatizar releases, especialmente en GitHub.

## Recomendaciones para el repo

1. Usar siempre el formato:

   ```text
   <type>(<scope>): <description>
   ```

   cuando haya un scope claro, o:

   ```text
   <type>: <description>
   ```

   cuando no lo haya.

2. Priorizar estos tipos iniciales:

   ```text
   feat, fix, docs, refactor, test, chore, build, ci, perf
   ```

3. Marcar breaking changes explícitamente con `!` o `BREAKING CHANGE:`.

4. Mantener la descripción en imperativo, breve y en minúscula cuando sea posible:

   ```text
   fix(auth): handle expired sessions
   docs: add setup guide
   ```

5. Considerar agregar commitlint si el equipo quiere validación automática.

## Fuentes

- [Conventional Commits 1.0.0](https://www.conventionalcommits.org/en/v1.0.0/)
- [Semantic Versioning 2.0.0](https://semver.org/)
- [commitlint config conventional](https://github.com/conventional-changelog/commitlint/tree/master/@commitlint/config-conventional)
- [commitlint docs](https://commitlint.js.org/)
- [conventional-changelog](https://github.com/conventional-changelog/conventional-changelog)
- [semantic-release](https://github.com/semantic-release/semantic-release)
