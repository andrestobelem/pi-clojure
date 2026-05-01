# Procedimiento para reiniciar con Pair Programming y TDD

## Objetivo

Cuando el diseño se vuelva confuso o la suite deje de expresar con claridad el
producto, podemos reiniciar la implementación y volver a avanzar en ciclos
pequeños de Pair Programming con TDD.

La idea no es borrar aprendizaje, sino separar conocimiento persistente de código
experimental. Conservamos documentación, decisiones, backlog y tooling; podemos
borrar `src/` y `test/` si eso ayuda a recuperar claridad de diseño.

## Working agreement

- Trabajamos como pair programming: la persona usuaria decide intención y
  prioridad; el agente propone el siguiente micro-paso y ejecuta cambios.
- Trabajamos de a una sola historia activa en GitHub Projects.
- Podemos crear una branch corta por story desde `main`, con formato `story/<issue-number>-<slug>`.
- La historia se mueve a `In Progress` al comenzar y vuelve a `Todo` si pausamos
  antes de escribir o conservar código.
- Cada cambio de comportamiento empieza con un test rojo visible.
- El green debe ser la solución más simple posible.
- La fase de refactor se hace con la suite en verde y no se saltea.
- Después de cada ciclo con checks verdes, hacemos un commit atómico.
- Si el diseño se confunde, podemos borrar `src/` y `test/`; no borramos
  documentación, decisiones, prompts, backlog ni tooling.

## Procedimiento

### 1. Elegir una sola historia

Antes de escribir código, elegir una única historia de usuario pequeña. Debe ser
la próxima capacidad observable del producto.

Formato:

```text
Como <usuario>, quiero <capacidad>, para <beneficio>.
```

### 2. Crear o verificar branch corta

Si vamos a usar branch por story, crearla desde `main` antes de escribir código:

```sh
git switch main
git pull --ff-only
git switch -c story/<issue-number>-<slug>
```

La branch debe vivir poco y volver rápido a `main`.

### 3. Red: escribir primero el test

Escribir un test pequeño desde el punto de vista del consumidor del código. El
test debe expresar el comportamiento deseado y fallar por una razón clara.

En esta fase diseñamos la API desde el test:

- nombres;
- argumentos;
- resultado esperado;
- errores observables;
- responsabilidades.

No escribir código productivo antes de ver el test fallar.

### 4. Green: implementar lo mínimo

Escribir la implementación más simple que haga pasar el test.

No buscar todavía generalidad, arquitectura perfecta ni features futuras. El
objetivo es obtener feedback rápido.

### 5. Refactor: mejorar diseño con la suite verde

Con los tests en verde, mejorar el diseño sin cambiar comportamiento observable.

Buscar:

- eliminar duplicación;
- mejorar nombres;
- separar responsabilidades;
- reducir acoplamiento;
- simplificar interfaces;
- mover lógica al namespace correcto.

La fase de refactor no es opcional. Según Dave Farley, TDD sin refactor puede
producir código cubierto por tests pero mal diseñado.

### 6. Commit: guardar el ciclo verde

Cuando el test pasa y la fase de refactor queda evaluada, correr los checks y
hacer un commit atómico con Conventional Commits.

No mezclar en ese commit cambios de otras historias o tareas no relacionadas.

### 7. Integrar a main

Al terminar la story, integrar rápido a `main` y borrar la branch de story.

Ejemplo local:

```sh
git switch main
git merge --ff-only story/<issue-number>-<slug>
git branch -d story/<issue-number>-<slug>
```

Si la integración no puede ser fast-forward, revisar por qué antes de mezclar
cambios.

### 8. Actualizar el proyecto

Después de cada ciclo:

- actualizar el estado de la historia en GitHub Projects;
- cerrar o comentar el issue si corresponde;
- si pausamos para retomar desde cero, devolver la historia a `Todo`;
- documentar decisiones durables en `docs/research/` o `docs/ideas/`;
- correr checks si hubo cambios posteriores al commit.

Checks actuales:

```sh
clj-kondo --lint src test
clojure -M:test
npx markdownlint-cli2 '**/*.md' '#node_modules'
```

## Reglas prácticas para este repo

- Trabajar de a una historia.
- Mantener ciclos de minutos, no horas.
- Si el test es difícil de escribir, tratarlo como feedback de diseño.
- Si el archivo de tests empieza a mezclar historias, refactorizar.
- Si el código productivo mezcla dominio, infraestructura y presentación,
  refactorizar.
- No seguir agregando features si la fase de refactor está pendiente.
- Preferir commits pequeños y atómicos.

## Referencias

- Kent Beck, *Test Driven Development: By Example*.
- Dave Farley, “Test Driven Development”.
- Dave Farley, “Three Distinct Mind-sets in TDD”.
- Dave Farley, enfoque de TDD como Test Driven Design.
- Steve Freeman y Nat Pryce, *Growing Object-Oriented Software, Guided by Tests*.
- Ver también:
  - [`2026-05-01-test-driven-design.md`](2026-05-01-test-driven-design.md)
  - [`2026-05-01-tdd-kent-beck-dave-farley.md`](2026-05-01-tdd-kent-beck-dave-farley.md)
