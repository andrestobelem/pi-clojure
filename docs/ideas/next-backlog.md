# Backlog candidato después de discovery paralelo

Fecha: 2026-05-01

Este backlog sintetiza y deduplica las propuestas de tres streams de discovery:
producto, dominio y UX/seguridad. No reemplaza al GitHub Project; sirve como
corte razonado para crear las próximas issues pequeñas.

## Principios de ordenamiento

- Priorizar demo end-to-end antes que infraestructura pesada.
- Mantener stories pequeñas, testeables y aptas para TDD.
- Evitar dos streams paralelos tocando intensivamente los mismos archivos.
- Separar validación bloqueante de Markdown de linting o sugerencias.
- Posponer snapshots, diff y contrato de persistencia hasta estabilizar export y
  permisos.

## Ahora

### 1. Mostrar errores accionables desde la CLI

**Story**: Como usuaria de CLI, quiero que un comando fallido muestre un error
accionable para corregir el problema sin ver stack traces.

**Criterios de aceptación**:

- Enviar Markdown inseguro muestra `Error: El mensaje contiene HTML crudo no
  permitido` o equivalente.
- El error incluye un código estable, por ejemplo `markdown/raw-html`.
- El error identifica el campo afectado, por ejemplo `message.body`.
- Usuario o sala inexistente producen mensajes claros.
- El proceso devuelve exit code distinto de cero cuando se ejecute como binario.
- El mensaje rechazado no aparece luego en `show` ni `export`.

**Labels sugeridas**: `type:feature`, `area:chat`, `area:markdown`,
`area:product`, `priority:high`.

### 2. Exportar una sala a un archivo Markdown

**Story**: Como usuario, quiero exportar una sala a un archivo `.md` para poder
compartir o versionar la conversación fuera de la CLI.

**Criterios de aceptación**:

- `export general andres --output general.md` escribe el documento Markdown en la
  ruta indicada.
- Si el archivo existe, no se sobrescribe salvo que se indique `--force`.
- La exportación por stdout sigue funcionando.
- El contenido escrito es igual al documento exportado por stdout.
- La salida confirma ruta, sala y cantidad de mensajes exportados.

**Labels sugeridas**: `type:feature`, `area:markdown`, `area:chat`,
`priority:high`.

### 3. Ver participantes activos de una sala

**Story**: Como participante de una sala, quiero ver quiénes están activos para
entender quién puede leer y responder la conversación.

**Criterios de aceptación**:

- Una sala compartida lista solo usuarios con participación activa.
- Un usuario que salió no aparece como participante activo.
- En una sala personal, el dueño aparece como participante activo.
- La respuesta incluye `user-id`, `handle` y `user-type`.
- No se exponen detalles internos de persistencia.

**Labels sugeridas**: `type:feature`, `area:domain`, `area:chat`,
`priority:high`.

### 4. Rechazar reintentos idempotentes incompatibles

**Story**: Como cliente de la CLI/API, quiero recibir un error claro si reutilizo
un `client-txn-id` para otro contenido para no ocultar inconsistencias.

**Criterios de aceptación**:

- Repetir `author-id + client-txn-id` con la misma sala y Markdown devuelve el
  mismo mensaje.
- Repetirlo con otro Markdown devuelve un error estructurado de conflicto
  idempotente.
- Repetirlo para otra sala devuelve error estructurado o queda fijado por test
  como clave distinta.
- El error no crea mensajes ni eventos nuevos.

**Labels sugeridas**: `type:feature`, `area:domain`, `area:chat`,
`priority:high`.

## Siguiente

### 5. Guion de demo end-to-end reproducible

**Story**: Como evaluador, quiero ejecutar un guion reproducible para ver el flujo
completo de chat Markdown exportable sin conocer comandos internos.

**Criterios de aceptación**:

- El guion crea usuarios, sala compartida y usa sala personal.
- Envía mensajes Markdown válidos, muestra conversación ordenada y exporta.
- Documenta comandos copiables y salida representativa.
- Falla de forma visible si algún paso de la demo no funciona.

**Labels sugeridas**: `type:docs`, `type:test`, `area:product`, `area:chat`,
`priority:medium`.

### 6. Agregar comando `validate-markdown`

**Story**: Como persona que integra scripts, quiero validar Markdown antes de
enviar para dar feedback temprano sin modificar estado.

**Criterios de aceptación**:

- `validate-markdown "Hola **mundo**"` imprime `Markdown válido`.
- `validate-markdown "<b>Hola</b>"` imprime error estructurado.
- El comando no crea ni modifica el archivo de estado.
- Usa el mismo resultado de dominio que `send`, sin duplicar reglas.

**Labels sugeridas**: `type:feature`, `area:markdown`, `area:product`,
`priority:medium`.

### 7. Registrar eventos de participación

**Story**: Como usuario, quiero que entradas y salidas queden auditadas para
reconstruir cómo evolucionó una sala.

**Criterios de aceptación**:

- `join` registra `:participation/joined`.
- `leave` registra `:participation/left`.
- Repetir `join` activo no duplica eventos.
- Repetir `leave` inactivo no duplica eventos o devuelve error explícito fijado
  por test.
- Los eventos conservan `room-id`, `actor-id` y tipo.

**Labels sugeridas**: `type:feature`, `area:domain`, `area:chat`,
`priority:medium`.

### 8. Exportar conversación con autor y metadatos mínimos

**Story**: Como lector del documento exportado, quiero ver autor y metadatos
mínimos para entender origen y trazabilidad.

**Criterios de aceptación**:

- La exportación incluye título y tipo de sala.
- Cada mensaje incluye secuencia y handle del autor.
- Si el autor es agente, se indica de forma simple.
- El cuerpo Markdown original no se normaliza ni renderiza a HTML.
- La salida es Markdown estable y testeable.

**Labels sugeridas**: `type:feature`, `area:markdown`, `area:domain`,
`priority:medium`.

## Después

### 9. Proteger exportación según acceso

**Story**: Como dueño o participante, quiero que exportar respete acceso a la sala
para no filtrar conversaciones privadas.

**Criterios de aceptación**:

- Un participante activo puede exportar una sala compartida.
- Un no participante no puede exportarla salvo regla explícita de sala pública.
- El dueño puede exportar su sala personal.
- Otro usuario no puede exportar una sala personal ajena.
- Los errores de acceso son explícitos en el dominio.

**Labels sugeridas**: `type:feature`, `area:domain`, `area:chat`,
`priority:medium`.

### 10. Política explícita para links e imágenes Markdown

**Story**: Como equipo de seguridad, quiero una política visible para links e
imágenes para reducir riesgos antes de habilitar contenido más rico.

**Criterios de aceptación**:

- Links `http` y `https` siguen permitidos.
- `javascript:`, `data:` y protocolos vacíos se rechazan con
  `markdown/unsafe-link`.
- Imágenes Markdown se rechazan inicialmente con `markdown/image-not-allowed`.
- Los errores explican una acción posible.
- Los casos quedan cubiertos por tests de dominio.

**Labels sugeridas**: `type:feature`, `area:markdown`, `priority:medium`.

### 11. Lint no bloqueante de mensajes

**Story**: Como autora, quiero recibir advertencias de legibilidad sin bloquear mi
conversación para mejorar el documento final gradualmente.

**Criterios de aceptación**:

- Un mensaje válido pero muy largo produce advertencia, no error, si está dentro
  del límite duro.
- Un bloque de código sin lenguaje produce advertencia.
- `send` persiste mensajes con solo advertencias y muestra éxito.
- Las advertencias usan severidad estable.
- Errores de seguridad siguen bloqueando.

**Labels sugeridas**: `type:feature`, `area:markdown`, `area:product`,
`priority:low`.

### 12. Snapshot simple de sala

**Story**: Como usuario, quiero crear un snapshot de una sala para fijar un punto
estable de la conversación.

**Criterios de aceptación**:

- El snapshot registra `room-id`, actor y última secuencia incluida.
- Crear snapshot no cambia mensajes existentes.
- La respuesta incluye identificador estable.
- Snapshot vacío se permite o se rechaza con error explícito fijado por test.
- No integra todavía Dolt commits reales.

**Labels sugeridas**: `type:feature`, `area:domain`, `area:dolt`, `area:chat`,
`priority:low`.

### 13. Comparar snapshots como diff Markdown básico

**Story**: Como lector, quiero comparar dos snapshots para entender qué cambió
entre versiones de una conversación.

**Criterios de aceptación**:

- La CLI muestra diferencias entre exportaciones Markdown de dos snapshots.
- El diff identifica mensajes agregados.
- Snapshots de salas distintas se rechazan con error claro.
- El formato puede ser textual básico.

**Labels sugeridas**: `type:feature`, `area:dolt`, `area:markdown`,
`priority:low`.

### 14. Contrato de persistencia del dominio

**Story**: Como equipo de producto, quiero que el dominio pueda migrar de
memoria/EDN a Dolt sin cambiar comportamientos visibles.

**Criterios de aceptación**:

- Existe una suite de contrato para crear usuarios, salas, join/leave, envío
  idempotente, lectura y exportación.
- El store en memoria pasa el contrato.
- La CLI sigue pasando con persistencia actual.
- El contrato no importa JDBC ni comandos Dolt.

**Labels sugeridas**: `type:test`, `area:domain`, `area:dolt`, `priority:low`.

## Recomendación de paralelización

- Stream A: errores CLI y `validate-markdown`.
- Stream B: participantes activos y eventos de participación.
- Stream review: export a archivo, demo script y refinamiento de criterios.

Evitar ejecutar en paralelo stories que cambien a la vez `export-room-markdown`,
la firma de `send-message!` o la representación de eventos.
