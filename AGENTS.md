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

Usar los tests como feedback de diseño, no solo como cobertura.

## Git

- `main` es la rama principal de integración.
- Evitar ramas de larga vida.
- Integrar cambios pequeños y frecuentes.
- Hacer commits pequeños, atómicos y con una intención clara.
- Usar Conventional Commits.

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

## Markdown

Después de editar Markdown, correr:

```sh
npx markdownlint-cli2 '**/*.md' '#node_modules'
```
