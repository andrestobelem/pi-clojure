# Decisiones iniciales del dominio

Este registro concentra decisiones durables del dominio que surgieron durante el
arranque TDD del MVP. Las exploraciones más amplias pueden vivir en otros
archivos de `docs/research/` o `docs/ideas/`; este documento captura solo lo que
queremos preservar como criterio de diseño.

## Conversación Markdown como núcleo

La unidad central del producto es una sala de conversación cuyo historial puede
leerse y exportarse como Markdown. El mensaje conserva `body-markdown` como
fuente de verdad; cualquier HTML renderizado será dato derivado.

Consecuencias:

- La validación ocurre antes de persistir o publicar mensajes.
- La exportación debe preservar el Markdown escrito por los participantes.
- El renderer HTML no define el modelo de dominio.

## Salas y mensajes append-only

El historial de una sala se modela como una secuencia ordenada de mensajes y
eventos. Para el MVP preferimos agregar eventos antes que mutar historial
existente.

Consecuencias:

- La conversación se reconstruye por orden de secuencia dentro de la sala.
- Las ediciones, eliminaciones o moderación quedan fuera del primer corte salvo
  que aparezcan como eventos explícitos futuros.
- El modelo queda alineado con una persistencia versionable como Dolt, sin
  acoplar el núcleo todavía a esa infraestructura.

## Usuarios humanos y agentes

El dominio distingue el tipo de usuario, pero humanos y agentes comparten las
mismas capacidades base al inicio. El tipo no otorga permisos especiales
implícitos.

Consecuencias:

- La autorización fina queda fuera del MVP inicial.
- Las reglas de sala y mensaje no deberían depender de una UI o de un canal de
  transporte específico.

## Sala personal automática

Crear un usuario humano debería crear una sala personal privada asociada a ese
usuario. La sala personal sirve como espacio inicial para capturar ideas sin
necesitar coordinación con otros usuarios.

Consecuencias:

- La creación de usuario y sala personal pertenece al flujo de dominio, no a una
  comodidad exclusiva de UI.
- La privacidad de la sala personal es una propiedad del modelo, aunque los
  permisos finos se diseñen más adelante.

## Idempotencia de envío

Enviar mensajes debe ser idempotente por `author-id` y `client-txn-id` para que
un reintento del cliente no duplique mensajes.

Consecuencias:

- El cliente debe enviar un identificador estable de transacción por intento
  lógico de mensaje.
- El dominio devuelve el mismo resultado para reintentos equivalentes.
- La idempotencia se resuelve antes de crear un nuevo mensaje o evento.

## Validación segura de Markdown

El MVP acepta un subconjunto seguro de Markdown y rechaza HTML crudo o contenido
con protocolos peligrosos.

Consecuencias:

- Los errores de validación deben ser explícitos y útiles para el cliente.
- El linting de estilo o legibilidad es una capa separada de la validación de
  seguridad/corrección.
- La lista exacta de nodos Markdown permitidos puede evolucionar cuando se elija
  un parser real.
