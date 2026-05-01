---
description: Finish one TDD user story and close its GitHub Project item
argument-hint: "<issue-number>"
---
# Finish TDD Story

Vamos a terminar la user story #$ARGUMENTS.

Objetivo: cerrar la historia solo si el ciclo TDD quedó completo y versionado.

Pasos:

1. Revisar `git status --short` y confirmar qué cambios pertenecen a la story.
2. Verificar que hubo ciclo completo:
   - test rojo observado;
   - green mínimo;
   - refactor evaluado con suite verde.
3. Correr checks:

   ```sh
   clj-kondo --lint src test
   clojure -M:test
   ```

   Si se editó Markdown, correr también:

   ```sh
   npx markdownlint-cli2 '**/*.md' '#node_modules'
   ```

4. Si hay cambios de la story sin commitear, hacer un commit atómico con
   Conventional Commits.
5. Obtener el hash del commit relevante:

   ```sh
   git log --oneline -1
   ```

6. Si estamos en una branch de story, integrarla rápido a `main` y borrar la branch:

   ```sh
   git switch main
   git merge --ff-only story/<issue-number>-<slug>
   git branch -d story/<issue-number>-<slug>
   ```

7. Mover el item del GitHub Project a `Done`.
8. Cerrar el issue con un comentario que incluya:
   - commit hash;
   - checks corridos;
   - cualquier decisión o fuera de alcance relevante.

Reglas:

- No cerrar la story si quedaron checks fallando.
- No mezclar cambios de otras historias en el commit de cierre.
- No cerrar la story si la fase de refactor quedó pendiente.
- No dejar branches de story vivas después de integrar a `main`.
- No guardar secretos, tokens ni datos privados en comentarios ni commits.
