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
4. Crear o verificar una branch corta desde `main` con formato `story/<issue-number>-<slug>`.
5. Proponer el primer micro-comportamiento observable.
6. Escribir primero un test rojo mínimo.
7. Mostrar el fallo.
8. Implementar el green más simple.
9. Correr checks:

   ```sh
   clj-kondo --lint src test
   clojure -M:test
   ```

10. Refactorizar solo con la suite verde.
11. Hacer un commit atómico con Conventional Commits para el ciclo verde.
12. Al terminar, integrar rápido a `main` y borrar la branch de story.
13. Al pausar o terminar, actualizar el Project y comentar o cerrar el issue.

Reglas:

- No avanzar a otra historia.
- No mantener branches de story de larga vida.
- No escribir código productivo antes del test rojo.
- No saltear refactor si el diseño lo pide.
- No dejar un ciclo verde sin commit atómico, salvo que la persona usuaria pida pausar antes de conservar cambios.
- Si el diseño se confunde, proponer borrar `src/` y `test/` antes de seguir.
- No guardar secretos, tokens ni datos privados.
