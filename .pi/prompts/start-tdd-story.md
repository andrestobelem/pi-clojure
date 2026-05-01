---
description: Start or resume one GitHub user story with strict Pair Programming TDD
argument-hint: "<issue-number>"
---
# Start TDD Story

Vamos a trabajar una sola user story: #$ARGUMENTS.

Objetivo: avanzar con Pair Programming y Test Driven Design estricto, siguiendo
`docs/research/2026-05-01-pairing-tdd-reset.md`.

Pasos:

1. Leer el issue y resumir la historia, criterios de aceptación y alcance.
2. Verificar el estado en GitHub Projects.
3. Si vamos a empezar, mover la historia a `In Progress`.
4. Proponer el primer micro-comportamiento observable.
5. Escribir primero un test rojo mínimo.
6. Mostrar el fallo.
7. Implementar el green más simple.
8. Correr checks:

   ```sh
   clj-kondo --lint src test
   clojure -M:test
   ```

9. Refactorizar solo con la suite verde.
10. Al pausar, actualizar el Project y comentar el issue.

Reglas:

- No avanzar a otra historia.
- No escribir código productivo antes del test rojo.
- No saltear refactor si el diseño lo pide.
- Si el diseño se confunde, proponer borrar `src/` y `test/` antes de seguir.
- No guardar secretos, tokens ni datos privados.
