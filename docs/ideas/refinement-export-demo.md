# Refinamiento de exportación y demo end-to-end

Fecha: 2026-05-01

Alcance de esta revisión: historias #31 y #34. No cambia código ni tests. El objetivo es dejar criterios y cortes de implementación suficientemente claros para que otros agentes puedan trabajar con TDD sin ambigüedad.

## Contexto revisado

- `git status --short --branch`: branch `review/refinar-export-demo`, sin cambios iniciales.
- #31: exportar una sala a un archivo Markdown.
- #34: guion de demo end-to-end reproducible.
- #30 y #32 están `In Progress` en worktrees separados.
- `README.md` define TDD, trunk-based development, uso de worktrees y comandos de checks.
- `docs/ideas/next-backlog.md` ubica #31 en `Ahora` y #34 en `Siguiente`.

## Historia #31: exportar una sala a un archivo Markdown

### Objetivo refinado de #31

Permitir que el mismo documento Markdown que hoy puede emitirse por stdout se escriba en una ruta explícita, sin sobrescribir archivos por accidente.

### Criterios de aceptación propuestos para #31

- `export general andres --output general.md` escribe el documento Markdown en `general.md`.
- Sin `--output`, `export general andres` mantiene el comportamiento actual por stdout.
- El contenido escrito en archivo es byte a byte igual al documento que se obtendría por stdout para la misma sala y estado.
- Si la ruta indicada ya existe, el comando falla de forma visible y no modifica el archivo.
- `export general andres --output general.md --force` sobrescribe un archivo existente.
- La confirmación de éxito al escribir archivo incluye ruta, sala y cantidad de mensajes exportados.
- La confirmación de éxito no se mezcla con el Markdown exportado por stdout.
- Si el directorio padre no existe o no es escribible, el comando falla sin crear un archivo parcial.
- El estado de chat no cambia por ejecutar `export`, tanto por stdout como por archivo.

### Casos de test sugeridos

1. Red CLI: exportar con `--output` crea un archivo con el Markdown esperado.
2. Red CLI/regresión: exportar sin `--output` imprime Markdown por stdout como antes.
3. Red CLI: archivo existente sin `--force` devuelve error y conserva el contenido previo.
4. Red CLI: archivo existente con `--force` reemplaza el contenido por la exportación actual.
5. Red CLI: la respuesta de éxito reporta cantidad de mensajes y ruta.
6. Red dominio/servicio si aplica: la cuenta de mensajes sale de la misma lectura usada para exportar, no de parsear el Markdown.

### Decisiones a fijar antes o durante TDD

- Orden de flags: aceptar `--output <path>` y `--force` en cualquier posición posterior a `room` y `handle`, o fijar explícitamente un orden simple.
- Canal de confirmación: se recomienda imprimir confirmación por stdout solo cuando se escribe a archivo; cuando se exporta por stdout, stdout debe contener solo Markdown.
- Formato mínimo de confirmación: por ejemplo `Exportado general a general.md (2 mensajes)`.
- Formato de error de sobrescritura: si #30 ya definió errores accionables, reutilizar su estilo y código estable, por ejemplo `export/file-exists`.

### Dependencias de #31

- Depende del comportamiento actual de `export` por stdout y de que exista una función estable de exportación Markdown.
- Conviene integrar o al menos revisar #30 antes de cerrar #31, para alinear los errores de archivo existente, ruta inválida y usuario/sala inexistente.
- No debería depender de #32 si exportar sigue usando las reglas actuales de lectura/acceso.

### Riesgos de conflicto de #31

- Con #30: ambos probablemente tocan parsing, salida y manejo de errores de CLI. Riesgo alto si se implementan en paralelo sobre `src/pi_clojure/cli.clj` y tests CLI.
- Con #32: riesgo bajo a medio. Si #32 cambia la representación de participantes o reglas de acceso de lectura, #31 debe evitar asumir detalles internos y solo usar APIs públicas.
- Con historias futuras de metadatos de exportación: #31 debe limitarse a destino de salida, no rediseñar el formato Markdown.

### Slicing recomendado para #31

La historia #31 es implementable como una historia pequeña si se evita ampliar el formato de exportación. Si aparece fricción, cortar en:

1. **#31a salida a archivo segura**: `--output`, no sobrescribir, stdout intacto.
2. **#31b sobrescritura explícita y confirmación**: `--force`, cantidad de mensajes y errores alineados con #30.

No incluir en #31 permisos finos, metadatos adicionales del documento, snapshots ni diff.

## Historia #34: guion de demo end-to-end reproducible

### Objetivo refinado de #34

Proveer un camino único, copiable y verificable para demostrar el MVP-1: crear usuarios y salas, enviar Markdown seguro, ver conversación ordenada y exportar a Markdown.

### Criterios de aceptación propuestos para #34

- Existe un guion reproducible, preferentemente `scripts/demo-export-chat.sh`, que se puede ejecutar desde un checkout limpio.
- El guion usa un archivo de estado temporal o aislado y no depende de `.pi-chat.edn` preexistente.
- El guion crea al menos dos usuarios humanos y una sala compartida.
- El guion demuestra la sala personal automática de un usuario sin requerir permisos finos nuevos.
- El guion envía al menos dos mensajes Markdown válidos a la sala compartida.
- El guion muestra la conversación ordenada antes de exportar.
- El guion exporta a un archivo Markdown usando la interfaz de #31.
- El guion verifica que el archivo exportado existe y contiene fragmentos esperados.
- El guion falla con exit code distinto de cero si cualquier paso falla.
- La documentación asociada incluye comandos copiables y salida representativa, sin depender de rutas absolutas locales.

### Casos de test o verificación sugeridos

1. Smoke test del script en CI/local: ejecutar el guion y esperar exit code 0.
2. Verificación negativa simple: si el comando CLI falla, `set -euo pipefail` corta el guion.
3. Verificación de artefacto: comprobar que el Markdown exportado contiene título de sala y mensajes enviados.
4. Verificación de aislamiento: correr el guion dos veces no falla por estado previo.

### Dependencias de #34

- Depende fuertemente de #31 para exportar a archivo.
- Se beneficia de #30 para que las fallas del guion sean legibles, pero no debería bloquearse si el script ya falla con exit code distinto de cero.
- Puede convivir con #32 si solo muestra participantes como paso opcional. No conviene hacer de participantes activos un criterio obligatorio de #34 salvo que #32 ya esté integrado.

### Riesgos de conflicto de #34

- Con #31: #34 debe esperar la interfaz final de `--output`/`--force` para no codificar comandos que cambien inmediatamente.
- Con #30: si #30 cambia `-main`, exit codes o canales stdout/stderr, el script debe ajustarse al contrato final.
- Con #32: riesgo bajo si el guion no requiere comando de participantes; riesgo medio si lo incorpora como parte obligatoria.
- Con README: #34 probablemente edite documentación pública; coordinar con agentes que estén tocando README para evitar conflictos triviales.

### Slicing recomendado para #34

La historia #34 puede ser grande si mezcla script ejecutable, documentación larga y fixtures. Cortar en:

1. **#34a guion smoke ejecutable**: script aislado, falla visible, exporta archivo y verifica contenido mínimo.
2. **#34b documentación de demo**: README o `docs/ideas/demo-export-chat.md` con comandos copiables y salida representativa.
3. **#34c endurecimiento opcional**: limpiar artefactos, hacer output más legible o parametrizar directorio temporal.

La primera rebanada debería ser suficiente para validar el flujo end-to-end; las otras pueden seguir como mejoras si el equipo quiere una demo más pulida.

## Orden sugerido

1. Terminar e integrar #30, o al menos congelar su contrato de errores CLI.
2. Implementar #31 con foco exclusivo en destino de exportación y seguridad de sobrescritura.
3. Implementar #34 usando la interfaz final de #31.
4. Integrar #32 en paralelo solo si no obliga a cambiar exportación ni demo; si introduce un comando de participantes, dejarlo opcional en la demo.

## Checklist para agentes implementadores

- Empezar cada historia con `git status --short --branch`.
- Crear branch por issue desde `main` usando `gh issue develop`.
- Escribir primero tests o smoke checks que fallen.
- No ampliar el alcance de #31 hacia formato Markdown enriquecido.
- No ampliar #34 hacia permisos, snapshots, diff o Dolt.
- Después de tocar Clojure: `clj-kondo --lint src test` y `clojure -M:test`.
- Después de tocar Markdown: `npx markdownlint-cli2 '**/*.md' '#node_modules'`.
