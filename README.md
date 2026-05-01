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

Para activar la validación en una copia nueva del repo:

```sh
git config core.hooksPath .githooks
```
