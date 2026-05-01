# Linting de mensajes y conversaciones

Además de validar Markdown por seguridad, el producto debería lintear mensajes y
conversaciones para mejorar legibilidad, consistencia y calidad documental.

La validación responde si un mensaje es permitido y seguro. El linting responde
si el contenido es claro, consistente y mantenible.

## Principio

El chat no debería volverse molesto ni bloquear innecesariamente la conversación.
Por eso las reglas deben distinguir entre errores bloqueantes, advertencias y
sugerencias.

```text
validación de seguridad -> puede bloquear
Markdown inválido       -> puede bloquear
estilo y consistencia   -> advierte
calidad documental      -> sugiere
```

## Alcances

### Mensaje individual

Se ejecuta antes de enviar o durante la edición del mensaje.

Ejemplos:

- Bloque de código sin lenguaje.
- Link sin texto descriptivo.
- Mensaje muy largo sin estructura.
- Encabezado demasiado alto para un mensaje corto.
- Lista con formato inconsistente.

### Conversación completa

Se ejecuta bajo demanda, al exportar o al crear snapshots.

Ejemplos:

- La conversación no tiene título claro.
- Hay decisiones detectadas sin resumen final.
- Hay preguntas abiertas sin responder.
- El documento exportado salta niveles de encabezados.
- Hay secciones duplicadas o contenido repetido.

## Tipos de reglas

### Markdown estructural

- No saltar niveles de encabezado sin razón.
- No usar múltiples `h1` en un documento exportado.
- Declarar lenguaje en bloques de código cuando sea posible.
- Mantener listas consistentes.
- Evitar links vacíos o ambiguos.

### Calidad documental

- Sugerir título para conversaciones largas.
- Detectar decisiones sin marca explícita.
- Detectar preguntas abiertas.
- Sugerir resumen cuando el hilo sea largo.
- Sugerir convertir una conversación extensa en documento.

### Estilo del producto

- Preferir mensajes largos con secciones.
- Sugerir dividir mensajes excesivamente grandes.
- Sugerir nombres claros para links.
- Evitar contenido difícil de exportar.

### Seguridad

Las reglas de seguridad pertenecen principalmente a validación, pero también
pueden reportarse en la misma interfaz de feedback:

- HTML crudo.
- Links con protocolos no permitidos.
- Embeds arbitrarios.
- Imágenes remotas sin política explícita.

## Modelo conceptual

Lint de mensaje:

```clojure
{:lint/scope :message
 :lint/rule :code-block/missing-language
 :lint/severity :warning
 :lint/message "El bloque de código no indica lenguaje"
 :lint/path [:blocks 2]}
```

Lint de conversación:

```clojure
{:lint/scope :conversation
 :lint/rule :decision/missing-summary
 :lint/severity :suggestion
 :lint/message "Hay decisiones detectadas sin resumen final"}
```

## Severidades

- `:error`: bloquea persistencia o publicación.
- `:warning`: no bloquea, pero se muestra al usuario.
- `:suggestion`: mejora opcional.
- `:info`: señal contextual o educativa.

## Momentos de ejecución

- Mientras el usuario escribe.
- Antes de enviar.
- Al editar un mensaje.
- Al exportar una sala como Markdown.
- Al crear un snapshot o commit.
- Bajo demanda mediante un bot o acción de sala.

## Relación con bots

Los bots pueden usar el linting para actuar como escribas o documentalistas:

- resumir problemas de estructura;
- proponer títulos;
- extraer preguntas abiertas;
- sugerir decisiones;
- preparar documentos Markdown exportables.

## Preguntas abiertas

- ¿Qué reglas deberían venir activadas por defecto?
- ¿Cada sala puede configurar sus reglas?
- ¿El usuario puede ignorar advertencias específicas?
- ¿Guardamos resultados de lint como eventos versionados?
- ¿El linting debe usar solo reglas determinísticas o también agentes/IA?
