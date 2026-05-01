# Validación de Markdown

Los mensajes deberían validar que el usuario envía Markdown aceptable antes de
persistirlos o distribuirlos a una sala.

La validación no debería intentar decidir si el texto está "bien escrito", sino
si cumple el subconjunto de Markdown permitido por el producto y si puede
renderizarse de forma segura. La calidad, consistencia y legibilidad pertenecen a
una capa separada de linting documentada en [`markdown-linting.md`](markdown-linting.md).

## Objetivos

- Evitar Markdown inválido o ambiguo que rompa la experiencia de lectura.
- Rechazar contenido peligroso antes de generar HTML.
- Mantener una representación Markdown canónica como fuente de verdad.
- Producir HTML sanitizado como dato derivado.
- Devolver errores útiles para que el cliente pueda mostrar feedback.

## Pipeline propuesto

```text
mensaje entrante
  -> validar tamaño
  -> parsear Markdown
  -> rechazar HTML crudo y nodos no permitidos
  -> validar links y protocolos
  -> normalizar Markdown si corresponde
  -> renderizar HTML
  -> sanitizar HTML
  -> persistir Markdown + HTML derivado
  -> publicar evento en la sala
```

## Subconjunto inicial permitido

- Párrafos.
- Negrita y cursiva.
- Tachado.
- Código inline.
- Bloques de código con lenguaje opcional.
- Listas ordenadas y desordenadas.
- Citas.
- Links `http` y `https`.
- Encabezados limitados.

## Contenido inicialmente no permitido

- HTML crudo.
- JavaScript URLs.
- `iframe`, `script`, `style` o embeds arbitrarios.
- SVG embebido.
- Imágenes remotas sin proxy o política explícita.
- Atributos HTML definidos por el usuario.

## Errores esperados

El servidor debería responder errores estructurados, por ejemplo:

```clojure
{:error/type :markdown/invalid
 :error/message "El mensaje contiene HTML crudo no permitido"
 :error/path [:blocks 2]}
```

## Preguntas abiertas

- ¿Conviene normalizar Markdown antes de guardar o preservar exactamente lo que
  escribió el usuario?
- ¿Qué renderer/parser Markdown usaremos en Clojure?
- ¿La validación debe ser igual para mensajes, documentos exportados y salas
  personales?
- ¿Permitiremos extensiones como tablas, checklists, menciones o Mermaid?
- ¿Cómo mostramos preview y errores antes de enviar?
