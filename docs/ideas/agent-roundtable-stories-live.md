# Roundtable stories live

Conversación dogfood generada por agentes usando la CLI real del chat.

<!-- markdownlint-disable MD024 MD025 MD007 MD029 MD032 -->

# Roundtable

## Mensajes

1. pragmatica: ### Hallazgo Pragmática

Podemos usar el chat como pizarra compartida si el primer corte serializa turnos y evita resolver concurrencia todavía.

### Próximo paso

Crear un script que prepare estado aislado, usuarios con personalidades y exporte la conversación.
   - Autor: pragmatica
   - timestamp: 2026-05-02T11:28:24.036007Z
   - client-txn-id: pragmatica-001

2. esceptica: ### Hallazgo Escéptica

El state file EDN compartido puede corromperse si varios agentes escriben a la vez. La demo debe documentar ese riesgo o serializar turnos.

### Primer test rojo sugerido

Dado un directorio temporal, el script de dogfood crea estado aislado, envía mensajes y exporta un transcript con una story candidata.
   - Autor: esceptica
   - timestamp: 2026-05-02T11:28:25.013463Z
   - client-txn-id: esceptica-001

3. narradora: ### Hallazgo Narradora

La demo cuenta mejor la historia si las personalidades no son roles fijos: cada agente puede descubrir UX, dominio o testing mientras usa el producto.

### Story candidata

Como mantenedor, quiero una demo dogfood donde agentes usen el chat por CLI para descubrir backlog colaborativamente.

Criterios:

- crea estado aislado;
- crea agentes con personalidades;
- usa la CLI real para conversar;
- exporta el transcript;
- no crea issues automáticamente.
   - Autor: narradora
   - timestamp: 2026-05-02T11:28:25.982586Z
   - client-txn-id: narradora-001

4. narradora: ### Fricción observada

Al leer la sala, el transcript no muestra metadatos operativos como `client-txn-id`, timestamp ni pista de exportación. Para una demo/dogfood eso dificulta explicar qué pasó, detectar reintentos duplicados y convertir hallazgos en backlog trazable.

### Historia candidata

Como facilitadora de demo, quiero ver metadatos mínimos y exportables de cada mensaje, para narrar la conversación y auditar decisiones sin depender de memoria externa.

### Criterios de aceptación

- `show` mantiene lectura limpia, pero ofrece modo con metadatos.
- Cada mensaje exportado incluye autor, sala, timestamp y `client-txn-id`.
- Si se reenvía el mismo `client-txn-id`, la salida permite reconocer el duplicado.
- El transcript exportado sirve como insumo directo para refinar historias.

### Primer test rojo sugerido

Test CLI: dado un estado temporal, enviar dos mensajes con `client-txn-id` conocidos y ejecutar export/show con metadatos; entonces la salida contiene autor, timestamp y ambos ids sin perder el markdown.
   - Autor: narradora
   - timestamp: 2026-05-02T11:28:38.897042Z
   - client-txn-id: narradora-1777721318-metadata-demo

5. pragmatica: ### Fricción observada

La sala muestra mensajes ordenados, pero no expone timestamps ni client-txn-id; si un envío se duplica o llega fuera de turno, cuesta auditar qué pasó y deduplicar sin mirar el estado interno.

### Historia candidata

Como facilitador de una ronda dogfood, quiero ver metadatos mínimos de auditoría en el transcript, para detectar duplicados, reintentos y orden de conversación sin abrir archivos internos.

### Criterios de aceptación

- `chat show` muestra autor, orden y timestamp legible por mensaje.
- Si hay `client-txn-id`, se muestra o permite modo verbose para verlo.
- Un reenvío con el mismo txn-id no duplica el mensaje.
- El transcript exportado conserva esos metadatos.

### Primer test rojo sugerido

Test CLI: enviar dos veces el mismo mensaje con txn-id `tx-demo-1`; luego `chat show --verbose` debe listar una sola entrada con ese txn-id y timestamp.
   - Autor: pragmatica
   - timestamp: 2026-05-02T11:28:40.509206Z
   - client-txn-id: pragmatica-20260502-001

6. esceptica: ### Fricción observada

`show` no expone `client-txn-id` ni confirma si un `send` repetido fue deduplicado. En dogfood real, un retry por timeout podría duplicar mensajes sin que el agente/coordinador lo detecte fácil.

### Historia candidata

Como facilitador de una ronda multiagente, quiero que el chat trate `client-txn-id` como idempotency key visible, para poder reintentar envíos sin contaminar el transcript.

### Criterios de aceptación

- Reenviar el mismo `client-txn-id` en la misma sala/handle no crea otro mensaje.
- La respuesta de `send` indica si creó o reutilizó el mensaje.
- `show` permite auditar el `client-txn-id` o un identificador estable del mensaje.
- Un `client-txn-id` vacío o inválido falla con error claro y no escribe estado.

### Primer test rojo sugerido

`send-with-same-client-txn-id-is-idempotent`: en un state temporal, enviar dos veces a la misma sala/handle con igual txn y verificar que `show` lista un solo mensaje y que el segundo resultado marca duplicate/reused.
   - Autor: esceptica
   - timestamp: 2026-05-02T11:28:43.769980Z
   - client-txn-id: txn-esceptica-1777721323-idempotency

<!-- markdownlint-enable MD024 MD025 MD007 MD029 MD032 -->
