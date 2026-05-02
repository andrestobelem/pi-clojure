# Evaluación de backlog web #56-#61

Fecha: 2026-05-02.

Rol: evaluator/backlog gardener para la tanda web #56-#61. Alcance de
implementación excluido: no se editó código ni `src/` ni `test/`. Las stories
Las stories #56 y #57 se observaron en modo lectura.

## Estado observado

- Worktree de evaluación: `review/evaluar-web-56-61`, limpio al iniciar.
- #56: `In Progress`, `Foco: Ahora`, `Canvas: Producto`, branch
  `story/56-web-crear-salas`.
- #57: `In Progress`, `Foco: Ahora`, `Canvas: Producto`, branch
  `story/57-web-crear-usuarios`.
- #58: `Todo`, `Foco: Siguiente`, `Canvas: Participación`.
- #59: `Todo`, `Foco: Siguiente`, `Canvas: Salas`.
- #60: `Todo`, `Foco: Después`, `Canvas: Producto`.
- #61: `Todo`, `Foco: Después`, `Canvas: Producto`.
- `clojure -M:chat audit-cycle` detecta dos streams activos, dentro del límite,
  pero advierte cambios pendientes en los worktrees de #56 y #57.

## Web actual

La web local actual tiene una home `GET /` que lista salas compartidas,
mensajes ordenados por secuencia y un formulario `POST /messages` por sala. Usa
el mismo state file EDN que la CLI, carga el store por request y solo guarda en
POST exitoso.

Capacidades existentes:

- render seguro con escape HTML;
- publicación de mensajes en salas existentes;
- validación de usuario existente y sala existente;
- rechazo de publicación si el usuario no participa, vía regla de dominio;
- error genérico seguro `No se pudo publicar` ante `ExceptionInfo`.

Huecos relevantes para #56-#61:

- no hay creación web de salas compartidas;
- no hay creación web de usuarios humanos;
- no hay acción web para unirse a una sala;
- no hay ruta dedicada por sala;
- los errores no muestran detalle accionable ni preservan inputs;
- no hay acción explícita de refresh GET.

## Observación de #56 y #57

Las stories #56 y #57 editan la misma zona de tests
(`test/pi_clojure/web_test.clj`) en paralelo. Ambas ramas agregaron tests rojos de formulario y handler. Esto es una
buena señal TDD, pero anticipa conflicto de integración simple por cercanía de
edición.

Recomendación operativa:

- mantener solo #56 y #57 en `In Progress` hasta que una se integre o pause;
- rebasear la segunda story sobre `main` apenas entre la primera;
- evitar abrir #58 hasta que los formularios base estén en `main`;
- no mover Project ni cerrar issues desde esta evaluación.

## Slicing recomendado

### #57 Crear usuarios desde web

Valor: habilita handles humanos locales sin CLI, prerequisito natural de join y
publicación web completa.

Slice mínimo:

1. Red de render: home muestra formulario `POST /users` con `handle`.
2. Red/green de handler puro: `create-user!` web crea usuario humano y sala
   personal usando `pi-clojure.domain.user/create-user!`.
3. Persistencia HTTP: ruta `POST /users` guarda solo con status exitoso.
4. Error seguro: handle inválido o duplicado no cambia el store y muestra texto
   accionable sin exponer IDs internos.

Riesgos:

- duplicar reglas de handle en la web en vez de delegar al dominio;
- persistir usuario sin sala personal ante error parcial;
- exponer `:user/id`, mapas EDN o datos internos en HTML;
- conflicto con #56 por render de formularios en home.

### #56 Crear salas compartidas desde web

Valor: permite iniciar conversaciones compartidas sin CLI.

Slice mínimo:

1. Red de render: home muestra formulario `POST /rooms` con título/nombre.
2. Red/green de handler puro: post válido crea sala compartida y la renderiza.
3. Persistencia HTTP: ruta `POST /rooms` guarda solo con status exitoso.
4. Error seguro: nombre inválido o duplicado no sobrescribe ni rompe la página.

Riesgos:

- `create-shared-room!` actual genera id por slug y puede sobrescribir si no se
  agrega una protección en el camino de dominio o aplicación;
- falta una política explícita de título de sala equivalente a handles;
- colisiones de slug por mayúsculas, espacios o títulos que normalizan igual;
- mezclar el slice con slugs editables o rutas por sala, que pertenecen a #59.

### #58 Unirse a una sala desde web

Valor: cierra el flujo mínimo usuario -> sala -> participación -> mensaje.

Slice mínimo:

1. Render: cada sala compartida muestra formulario `POST /rooms/:id/join` o
   equivalente con handle existente.
2. Handler puro: usuario existente queda participante activo con
   `join-room!`.
3. Persistencia HTTP: post exitoso guarda y re-renderiza permitiendo publicar.
4. Error seguro: usuario inexistente o sala inválida no revela participantes ni
   mensajes privados.

Riesgos:

- si se implementa antes de #56/#57, obliga a fixtures por CLI y aporta menos
  valor de UX;
- no conviene mostrar listas de participantes si eso expande alcance;
- el formulario por sala en home puede saturar la página hasta que exista #59.

### #59 Página dedicada por sala

Valor: reduce ruido de home, prepara links compartibles locales y mejora futuros
refreshes.

Slice mínimo:

1. Ruta estable para sala compartida, derivada de id/slug ya existente.
2. Home enlaza cada sala a su página.
3. Página de sala muestra solo esa sala, mensajes ordenados y formularios
   relevantes.
4. Sala inexistente devuelve 404 seguro.

Riesgos:

- los slugs actuales salen del título y no tienen una política fuerte de
  unicidad; conviene apoyarse en ids existentes o resolver #56 primero;
- duplicar render entre home y página de sala sin extraer componentes pequeños;
- filtrar información de salas no compartidas si la ruta acepta cualquier id.

### #60 Errores visibles y accionables

Valor: convierte errores genéricos en feedback útil para uso real.

Slice mínimo:

1. Diseñar una estructura de error por formulario: mensaje público, campo y
   valores preservables.
2. Cubrir Markdown inválido, handle inválido y participación requerida.
3. Mostrar detalle seguro cerca del formulario correspondiente.
4. Confirmar que errores no persisten cambios.

Riesgos:

- mostrar directamente mensajes técnicos de excepciones con datos sensibles;
- preservar campos sensibles o ids internos;
- tocar todos los formularios a la vez y agrandar el conflicto con #56/#57/#58;
- introducir i18n o diseño visual avanzado fuera de alcance.

### #61 Refresh manual sin perder contexto

Valor: mejora dogfood sin WebSockets ni polling.

Slice mínimo:

1. Renderizar enlace/botón GET de refresh en home y/o página de sala actual.
2. Asegurar patrón POST/redirect/GET o respuesta con link GET para no reenviar
   formularios accidentalmente.
3. Documentar explícitamente que todavía no hay tiempo real.
4. Testear que el link apunta a la vista actual.

Riesgos:

- si se hace antes de #59, puede quedar limitado a home y requerir retoque luego;
- confundir refresh manual con polling automático;
- mantener respuestas POST en una página que el navegador puede reenviar.

## Orden de integración recomendado

1. Integrar #57 primero si queda verde: habilita el handle humano, que es el
   primer paso del flujo de participación.
2. Integrar #56 inmediatamente después, rebasada sobre `main`, resolviendo el
   conflicto de render/tests de home.
3. Integrar #58 cuando #56 y #57 estén en `main`: completa el flujo end-to-end
   para publicar desde web sin CLI.
4. Integrar #59: mueve formularios y lectura a una página por sala más clara.
5. Integrar #60: endurece la UX de errores sobre todos los formularios ya
   existentes, evitando re-trabajo prematuro.
6. Integrar #61: agrega refresh manual sobre la navegación final, idealmente con
   páginas dedicadas ya disponibles.

Si #56 termina antes que #57, puede entrar primero sin bloquear, pero la segunda
story debe rebasar temprano porque ambas tocan la misma home y tests.

## Decisión de backlog

- No crear nuevas stories durante esta tanda.
- No cerrar issues #56-#61 desde la evaluación.
- Mantener #56 y #57 como únicas stories activas.
- Usar #58 como siguiente corte de producto cuando haya capacidad.
- Mantener #60 y #61 en `Después` hasta observar fricción real sobre los
  formularios ya integrados.
