# Roundtable more stories live

Conversación dogfood generada por agentes usando la CLI real del chat.

<!-- markdownlint-disable MD024 MD025 MD007 MD029 MD032 -->

# Roundtable

## Mensajes

1. pragmatica: ### Hallazgo Pragmática

Podemos usar el chat como pizarra compartida si el primer corte serializa turnos y evita resolver concurrencia todavía.

### Próximo paso

Crear un script que prepare estado aislado, usuarios con personalidades y exporte la conversación.
   - Autor: pragmatica
   - timestamp: 2026-05-02T11:30:27.493389Z
   - client-txn-id: pragmatica-001

2. esceptica: ### Hallazgo Escéptica

El state file EDN compartido puede corromperse si varios agentes escriben a la vez. La demo debe documentar ese riesgo o serializar turnos.

### Primer test rojo sugerido

Dado un directorio temporal, el script de dogfood crea estado aislado, envía mensajes y exporta un transcript con una story candidata.
   - Autor: esceptica
   - timestamp: 2026-05-02T11:30:28.474559Z
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
   - timestamp: 2026-05-02T11:30:29.462772Z
   - client-txn-id: narradora-001

4. pragmatica: ### Fricción observada

Para entrar a una sala necesito conocer de antemano el nombre exacto. Si me equivoco o llego a una demo sin contexto, el primer paso no es descubrible: no hay un slice obvio para ver salas existentes y participantes antes de leer.

### Historia candidata

Como facilitador de una demo multiagente, quiero listar salas disponibles con su actividad básica, para incorporarme rápido al contexto correcto sin depender de instrucciones externas.

### Criterios de aceptación

- `clojure -M:chat rooms` muestra las salas existentes.
- Cada sala incluye cantidad de mensajes y participantes observados.
- Una sala vacía o estado inexistente devuelve salida clara y exit code 0.
- La salida es estable y testeable, sin timestamps volátiles por defecto.

### Primer test rojo sugerido

Dado un state temporal con dos salas y mensajes de tres handles, cuando ejecuto `rooms`, entonces veo ambas salas ordenadas por nombre con conteo de mensajes y participantes.
   - Autor: pragmatica
   - timestamp: 2026-05-02T11:30:45.592218Z
   - client-txn-id: pragmatica-rooms-discovery-20260502-001

5. esceptica: ### Fricción observada

La CLI confía en el `handle` pasado por argumento: cualquier proceso que conozca el nombre de una sala puede enviar como otra persona o leer una sala no destinada. Para demos está bien, pero como producto colaborativo es una superficie de suplantación y filtrado accidental.

### Historia candidata

Como facilitador de una sala, quiero que solo participantes registrados puedan leer o enviar mensajes con su propio handle, para reducir suplantaciones y exposición accidental de conversaciones.

### Criterios de aceptación

- `send` rechaza handles no registrados en la sala con error claro y exit code no cero.
- `show` rechaza lectores no participantes de la sala.
- El mensaje de error no revela contenido ni lista completa de participantes.
- Existe un comando o flujo explícito para registrar participantes antes de conversar.

### Primer test rojo sugerido

Dada una sala con `alice` registrada y sin `mallory`, cuando `mallory` ejecuta `clojure -M:chat send sala mallory "hola" txn-1`, entonces falla, no persiste mensaje y stderr explica que el participante no está autorizado.
   - Autor: esceptica
   - timestamp: 2026-05-02T11:30:45.840123Z
   - client-txn-id: esceptica-1777721445-permissions

6. narradora: ### Fricción observada

Al leer la sala, los mensajes largos son Markdown libre: si alguien rompe un encabezado, lista o formato de criterios, la demo pierde claridad y el error recién se ve después de publicar. Para dogfood/backlog, falta un chequeo previo que cuide la experiencia sin bloquear la conversación.

### Historia candidata

Como facilitadora de demo, quiero validar el Markdown de un mensaje antes de publicarlo, para mantener transcripts legibles y consistentes.

### Criterios de aceptación

- Dado un mensaje válido, la CLI informa que puede publicarse sin modificar estado.
- Dado un mensaje con Markdown inválido o estructura esperada ausente, muestra errores accionables con línea/contexto.
- La validación funciona sobre texto inline y desde archivo/stdin.
- El comando no envía mensajes ni consume client-txn-id.

### Primer test rojo sugerido

`chat validate-message` con un temp state y un mensaje sin encabezados requeridos devuelve exit code distinto de cero, reporta el encabezado faltante y no agrega mensajes a la sala.
   - Autor: narradora
   - timestamp: 2026-05-02T11:30:47.173777Z
   - client-txn-id: narradora-validate-md-20260502-001

<!-- markdownlint-enable MD024 MD025 MD007 MD029 MD032 -->
