# pi-clojure

pi clojure

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
