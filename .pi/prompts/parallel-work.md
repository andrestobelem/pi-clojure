---
description: Plan parallel work with two implementation pi instances and one reviewer pi
argument-hint: "<issue-a> <issue-b> [review-scope]"
---
# Parallel pi work

Queremos coordinar trabajo paralelo con múltiples instancias de pi.

Objetivo: preparar dos streams de implementación y un stream de revisión sin
romper trunk-based development ni mezclar cambios.

Inputs:

- Story A: #$ARGUMENTS
- Story B: #$ARGUMENTS
- Scope de revisión opcional: #$ARGUMENTS

Pasos:

1. Confirmar que el repo principal está limpio:

   ```sh
   git status --short --branch
   ```

2. Verificar o crear worktrees:

   ```sh
   git worktree list
   git worktree add ../pi-clojure-a main
   git worktree add ../pi-clojure-b main
   git worktree add ../pi-clojure-review main
   ```

3. Para cada story de implementación:
   - leer el issue;
   - confirmar que no se solapa fuertemente con la otra story;
   - crear branch con `gh issue develop` dentro del worktree correspondiente;
   - marcar la story `In Progress` en GitHub Project;
   - escribir primero un test rojo.

4. Para el pi reviewer:
   - no modificar código de stories activas;
   - revisar issues abiertos y Project;
   - mejorar criterios de aceptación;
   - detectar duplicados o stories parcialmente cubiertas;
   - crear issues de refactor/docs si hace falta;
   - si modifica archivos, crear una branch propia.

5. Reglas para todos los pis:
   - una story activa por worktree;
   - commits atómicos;
   - correr checks antes de cerrar;
   - integrar a `main` con fast-forward;
   - borrar branches locales y remotas al terminar;
   - no commitear secretos ni datos privados.

Mensajes iniciales sugeridos:

```text
Sos pi-dev-a. Trabajá solo en la story #<issue-a> en este worktree. Seguí TDD:
Red, Green, Refactor, Commit. No toques otras stories. Antes de cerrar, corré
checks, actualizá issue/project e integrá a main con fast-forward.
```

```text
Sos pi-dev-b. Trabajá solo en la story #<issue-b> en este worktree. Evitá tocar
archivos de la story de pi-dev-a salvo coordinación explícita. Seguí TDD y
commits atómicos.
```

```text
Sos pi-review. No edites código de stories activas. Revisá backlog, issues y
GitHub Project. Mejorá criterios, detectá duplicados, partí stories grandes y
creá/refiná issues. Si necesitás modificar archivos, creá tu propia branch.
```
