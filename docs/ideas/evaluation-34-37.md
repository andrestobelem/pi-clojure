# Evaluación de integración de historias 34 y 37

Fecha: 2026-05-02

Rol: reviewer/evaluator/backlog gardener. Alcance: no implementar funcionalidad y no
editar `src/` ni `test/`.

## Estado observado

- Rama de revisión: `review/evaluar-34-37`.
- #34: `story/34-demo-end-to-end`, con commit `a51ab28` y working tree limpio.
- #37: `story/37-exportaciones-autor-metadatos`, con commit `33d4cb7` y working
  tree limpio.
- Ambas ramas parten de `0cf020a` y están detrás de `main` por
  `5406fc4 docs(research): document agent workflow patterns`.
- `git merge-tree main story/34-demo-end-to-end` no reportó conflictos.
- `git merge-tree main story/37-exportaciones-autor-metadatos` no reportó
  conflictos.
- `git merge-tree story/34-demo-end-to-end story/37-exportaciones-autor-metadatos`
  no reportó conflictos.

## Checklist de evaluación para #34

### Alcance de #34

- [x] Incluye guion reproducible de demo end-to-end.
- [x] Usa estado temporal/aislado mediante `DEMO_TMP_DIR` y `PI_CHAT_STATE_FILE`.
- [x] Crea dos usuarios humanos, sala compartida y demuestra sala personal.
- [x] Envía Markdown válido, muestra conversación ordenada y exporta a archivo.
- [x] Verifica existencia/contenido del artefacto exportado y falla si algo no
  cumple.
- [ ] Riesgo de alcance: introduce soporte productivo para `PI_CHAT_STATE_FILE`
  en la CLI. Parece justificado para aislamiento, pero conviene que el
  implementador confirme que se acepta como contrato público o lo documente como
  variable de entorno soportada.

### TDD y cobertura de #34

- [x] Agrega `pi-clojure.demo-script-test` que ejecuta el script real.
- [x] El test valida salida visible, archivo exportado y fragmentos esperados.
- [ ] Edge pendiente sugerido: documentar o testear explícitamente que ejecutar
  el script dos veces con el mismo `DEMO_TMP_DIR` sigue siendo idempotente para
  la demo. El script borra `state.edn` y `general-export.md`, por lo que debería
  pasar.

### Checks ejecutados para #34

- [x] `clj-kondo --lint src test` en el worktree de #34.
- [x] `clojure -M:test` en el worktree de #34.
- [x] `npx markdownlint-cli2 '**/*.md' '#node_modules'` en el worktree de #34.

### Edge cases de #34

- Estado local del desarrollador no debe leerse ni modificarse.
- Rutas con espacios en `DEMO_TMP_DIR`: el script cita las variables principales.
- Fallo visible si no existe `clojure`, si falla un comando de chat o si no se
  genera el export.
- Compatibilidad futura con #37: la demo no debería depender del formato exacto
  completo del export, solo de fragmentos estables.

## Checklist de evaluación para #37

### Alcance de #37

- [x] La exportación incluye título de sala.
- [x] La exportación incluye tipo de sala estable: `personal` o `shared`.
- [x] Cada mensaje incluye secuencia visible.
- [x] Cada mensaje incluye handle del autor.
- [x] Un autor agente se muestra como `handle (agent)`.
- [x] El cuerpo Markdown original se conserva como Markdown, sin HTML renderizado.
- [x] La salida es determinista para el mismo estado.
- [x] No agrega escritura a archivo, permisos nuevos ni exportación de eventos.

### TDD y cobertura de #37

- [x] Tests de dominio cubren sala compartida, sala personal, autor humano,
  autor agente, preservación de Markdown y determinismo.
- [x] Tests CLI actualizan el contrato de stdout y archivo para usar el mismo
  Markdown enriquecido.
- [ ] Edge pendiente sugerido: evaluar si `export-author-label` debería fallar
  con error claro cuando el autor no existe, o si ese estado inválido queda fuera
  del dominio.

### Checks ejecutados para #37

- [x] `clj-kondo --lint src test` en el worktree de #37.
- [x] `clojure -M:test` en el worktree de #37.
- [x] `npx markdownlint-cli2 '**/*.md' '#node_modules'` en el worktree de #37.

### Edge cases de #37

- Sala sin mensajes: debe conservar encabezado, tipo y sección `Mensajes` de
  forma estable.
- Mensajes con listas, énfasis y bloques de código: deben permanecer como cuerpo
  Markdown original.
- Eventos de participación agregados por #36 no deben aparecer en el documento.
- Secuencia exportada debe ser la secuencia visible de mensajes, no la posición
  de eventos.

## Conflictos probables y orden de integración

### Conflictos técnicos

- No se detectan conflictos de merge entre `main`, #34 y #37 con `merge-tree`.
- #34 toca `src/pi_clojure/cli.clj` y agrega docs/script/test de demo.
- #37 toca `src/pi_clojure/domain/user.clj` y tests de exportación.
- Solapamiento funcional: #34 documenta salida representativa del export; #37
  cambia el formato enriquecido del export.

### Orden sugerido

1. Integrar primero #37 para fijar el formato final de exportación con metadata.
2. Rebasar #34 sobre `main` actualizado con #37.
3. Ajustar en #34, si el implementador lo acepta, la salida representativa de
   `docs/demo-export-chat.md` para mostrar `Tipo:` y `Autor:` del formato final.
4. Ejecutar checks de #34 rebasada.
5. Integrar #34.
6. Ejecutar checks finales en `main` después de ambos merges.

## Hallazgos para reportar

- **#34 / documentación:** si #37 entra primero, la sección "Primeras líneas del
  Markdown exportado" de `docs/demo-export-chat.md` queda potencialmente
  desactualizada porque el export incluirá `Tipo: shared` y `Autor: ...`. Pedir
  al implementador de #34 rebasear y actualizar esa salida representativa antes
  del merge final.
- **#34 / alcance:** `PI_CHAT_STATE_FILE` es una mejora de CLI necesaria para
  aislamiento de demo, pero es comportamiento productivo nuevo. Pedir confirmación
  de que queda como contrato intencional y, si corresponde, documentarlo cerca de
  los comandos de uso.
- **#37 / robustez:** considerar si autores inexistentes en mensajes deben tener
  error explícito. No bloquea si el modelo garantiza integridad referencial.

## Checks ejecutados desde este rol

En la rama de revisión local:

```sh
clj-kondo --lint src test
clojure -M:test
npx markdownlint-cli2 '**/*.md' '#node_modules'
```

En `../pi-clojure-34-demo-end-to-end`:

```sh
clj-kondo --lint src test
clojure -M:test
npx markdownlint-cli2 '**/*.md' '#node_modules'
```

En `../pi-clojure-37-export-metadata`:

```sh
clj-kondo --lint src test
clojure -M:test
npx markdownlint-cli2 '**/*.md' '#node_modules'
```

Todos los checks anteriores pasaron.
