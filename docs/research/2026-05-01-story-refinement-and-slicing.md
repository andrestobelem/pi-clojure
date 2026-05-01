# Refinamiento y slicing de stories

## Contexto

Durante el avance del dominio varias stories quedaron parcialmente cubiertas por
slices anteriores. Por ejemplo, la participación en salas ya incluía lectura y
envío básicos, y luego las stories de enviar mensajes y leer conversación
necesitaron refinamiento para no duplicar trabajo ni perder intención de
producto.

## Decisión

Antes de implementar una story que parece ya cubierta, revisar si todavía aporta
una señal útil:

- si aporta una garantía de producto no explicitada, acotarla y convertirla en
  tests o documentación específica;
- si es duplicada, cerrarla o absorberla en la historia que ya la cubrió;
- si mezcla varias responsabilidades, dividirla en slices más pequeños;
- si revela una deuda de diseño, crear una tarea de refactor separada.

## Criterios para conservar una story parcialmente cubierta

Conviene conservarla si permite verificar explícitamente al menos uno de estos
puntos:

- una regla de acceso o autorización;
- una propiedad de orden, unicidad o monotonicidad;
- preservación de datos importantes, como Markdown fuente;
- independencia entre agregados, por ejemplo secuencias por sala;
- un evento de dominio requerido por historias posteriores.

## Criterios para cerrar o absorber

Conviene cerrar o absorber una story si:

- sus criterios ya están cubiertos por tests claros en otra story;
- no agrega comportamiento observable;
- solo repite una tarea técnica sin valor de producto;
- mantenerla abierta confunde el board o el foco del MVP.

## Flujo recomendado

1. Leer el issue y comparar criterios contra código y tests actuales.
2. Actualizar el cuerpo del issue con una sección de decisión de refinamiento.
3. Ajustar criterios de aceptación para que expresen el slice real.
4. Implementar solo el mínimo test/código faltante.
5. Cerrar la story con un comentario que explique si fue refinada, absorbida o
   implementada directamente.

## Aprendizaje

Las stories no son contratos rígidos: son hipótesis de valor. En TDD conviene
mantenerlas pequeñas y observables. Si una historia queda solapada por el camino,
el mejor resultado suele ser preservar la intención como test explícito o limpiar
el backlog para reducir ruido.
