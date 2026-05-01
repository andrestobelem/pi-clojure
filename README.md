# pi-clojure

pi clojure

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

### Trunk-based development

`main` es la rama principal de integración.

Buenas prácticas:

- Integrar cambios pequeños en `main` frecuentemente.
- Evitar ramas de larga vida.
- Si usamos ramas, que sean cortas y vuelvan rápido a `main`.
- Mantener `main` en estado consistente.
- Usar feature flags o branch by abstraction para cambios grandes.

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
