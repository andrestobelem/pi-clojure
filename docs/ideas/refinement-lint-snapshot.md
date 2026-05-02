# Refinamiento de lint no bloqueante y snapshots

Fecha: 2026-05-02

Rol: backlog gardener/reviewer. No modifica issues, Project, `src/` ni `test/`.

## Contexto operativo

- Rama/worktree revisado: `review/refinar-40-41`.
- Issues revisadas: #40, #41, #38 y #39.
- Documentos base: `docs/ideas/next-backlog.md` y
  `docs/research/2026-05-01-agent-patterns-backlog-and-implementation.md`.
- #38 y #39 están en `In Progress`; #40 y #41 siguen en `Todo` y deben esperar a
  que se estabilicen las decisiones de exportación/acceso y Markdown seguro.

## #40 Lint no bloqueante de mensajes

### Intención refinada para #40

Separar validación bloqueante de seguridad/contrato de un lint de legibilidad que
produzca advertencias estables y no impida persistir mensajes válidos.

### Slicing sugerido para #40

1. Dominio puro de lint Markdown:
   - Nueva función sin efectos que reciba `body-markdown` y devuelva colección de
     advertencias.
   - No cambia `validate-message-markdown!` ni el contrato de errores actuales.
2. Integración con envío:
   - `send-message!` puede devolver el mensaje con metadatos de advertencias o una
     respuesta envoltorio, pero debe preservarse idempotencia.
   - Si se reutiliza `client-txn-id`, las advertencias del retry compatible deben
     ser consistentes con el mensaje ya persistido o recalculables de forma pura.
3. UX CLI mínima:
   - `send` sigue mostrando éxito y añade advertencias legibles cuando existan.
   - `validate-markdown` puede mantenerse como validación bloqueante; un comando de
     lint separado queda fuera de este slice salvo decisión explícita.

### Criterios propuestos para implementación de #40

- Un bloque cercado como ```` ```\ncode\n``` ```` devuelve una advertencia con
  `:warning/type :markdown/code-block-without-language`,
  `:warning/severity :warning.severity/info` o severidad estable equivalente, y
  `:warning/path [:message/body]`.
- Un mensaje largo pero menor o igual que el límite duro devuelve una advertencia
  de legibilidad y no un error.
- Un mensaje mayor que el límite duro sigue devolviendo error
  `:markdown/too-long` y no se persiste.
- HTML crudo, links inseguros e imágenes rechazadas por #39 siguen bloqueando.
- `send` persiste mensajes con solo advertencias y comunica éxito.
- Las advertencias no cambian el Markdown original ni el orden de mensajes.

### Primer test rojo más concreto para #40

En tests de dominio Markdown:

```clojure
(deftest lint-message-markdown-warns-about-code-block-without-language
  (is (= [{:warning/type :markdown/code-block-without-language
           :warning/severity :warning.severity/info
           :warning/path [:message/body]}]
         (markdown/lint-message-markdown "```\n(+ 1 1)\n```"))))
```

Luego, un test de integración de envío debería crear una sala, enviar ese cuerpo y
verificar que el mensaje queda persistido aunque la respuesta incluya la
advertencia.

### Dependencias y riesgos para #40

- Depende de #39 para no confundir advertencias con reglas de seguridad.
- Puede tocar `src/pi_clojure/domain/markdown.clj`, `src/pi_clojure/domain/user.clj`
  y `src/pi_clojure/cli.clj`, archivos calientes para #39 y para UX CLI.
- Riesgo de diseño: si `send-message!` cambia de devolver mensaje a devolver
  envoltorio, muchos tests existentes pueden requerir ajuste. Preferir agregar
  metadatos o una función nueva de caso de uso antes de romper contrato.
- Riesgo de idempotencia: las advertencias no deben crear eventos nuevos ni
  cambiar el resultado de un retry compatible.

## #41 Snapshot simple de sala

### Intención refinada para #41

Crear un snapshot lógico y local de una sala, sin integrar todavía commits Dolt,
para fijar qué secuencia de mensajes queda incluida en un punto estable.

### Slicing sugerido para #41

1. Dominio in-memory:
   - Agregar registro de snapshots al store.
   - Crear snapshot con `room-id`, `actor-id`, última secuencia incluida e
     identificador estable.
2. Reglas de acceso:
   - El actor debe existir.
   - La sala debe existir.
   - La regla de autorización debe seguir la decisión de #38 si el snapshot se
     expone desde CLI o usa lectura/exportación.
3. CLI opcional en slice posterior:
   - `snapshot <room> <handle>` puede esperar si el objetivo inicial es fijar el
     contrato de dominio.

### Criterios propuestos para implementación de #41

- `create-room-snapshot!` devuelve un mapa con `:snapshot/id`,
  `:snapshot/room-id`, `:snapshot/actor-id` y `:snapshot/last-sequence`.
- En una sala con mensajes 1..N, `:snapshot/last-sequence` es N.
- Crear snapshot no modifica mensajes, participaciones ni eventos de mensaje.
- El identificador es estable y testeable; por ejemplo
  `snapshot:<room-id>:<last-sequence>` o incluye actor si se decide evitar
  colisiones por actor.
- La política para snapshot vacío queda fijada por test: recomendación de backlog,
  permitirlo con `:snapshot/last-sequence 0` para simplificar demo y evitar error
  artificial.
- No ejecuta `dolt commit`, no depende de JDBC y no escribe fuera del store actual.

### Primer test rojo más concreto para #41

En tests de dominio de sala/chat:

```clojure
(deftest create-room-snapshot-captures-last-message-sequence
  (let [store (chat/create-store)
        user (chat/create-user! store "andres" :user.type/human)
        room (chat/create-shared-room! store "General")]
    (chat/join-room! store (:user/id user) (:room/id room))
    (chat/send-message! store (:user/id user) (:room/id room) "Hola" "txn-1")
    (chat/send-message! store (:user/id user) (:room/id room) "Chau" "txn-2")
    (is (= 2
           (:snapshot/last-sequence
            (chat/create-room-snapshot! store
                                        (:user/id user)
                                        (:room/id room)))))))
```

Test complementario recomendado: crear snapshot de sala vacía y esperar
`:snapshot/last-sequence 0`.

### Dependencias y riesgos para #41

- #41 debe esperar a #38 si se decide que snapshot requiere autorización parecida
  a export/read. Implementarlo antes puede fijar reglas incompatibles.
- Puede tocar `src/pi_clojure/domain/user.clj`, archivo caliente para #38.
- El label `area:dolt` puede inducir sobre-implementación; mantener explícito que
  no hay commits Dolt reales en este slice.
- Si #38 cambia la firma de exportación para incluir actor, conviene reutilizar la
  misma función de autorización en snapshot para no duplicar reglas.

## Vigilancia de riesgos #38/#39

### #38 Exportar respetando acceso

- Hoy `export` resuelve usuario pero no usa su id al llamar a exportación. La story
  probablemente necesita cambiar firma de dominio/CLI para incluir actor.
- Posible conflicto directo con #41 en reglas de acceso a salas y con cualquier
  refactor de `read-room`/`export-room-markdown`.
- Cuidar que los errores sean `ExceptionInfo` con `:error/type` y `:error/path`
  para que la CLI los muestre de forma accionable.
- Verificar que una sala compartida no quede accidentalmente exportable por
  usuarios existentes pero no participantes.

### #39 Política de links e imágenes Markdown

- Puede cambiar `src/pi_clojure/domain/markdown.clj`, que también necesitará #40.
- Conviene completar #39 antes de #40 para que el lint no mezcle severidades con
  errores bloqueantes.
- Caso de protocolo vacío en links necesita definición precisa: `[x](:foo)` o
  `[x](//example.com)` deben quedar cubiertos por tests si el criterio los nombra.
- Imágenes Markdown (`![alt](url)`) deberían rechazarse antes de evaluar si el link
  es `http` válido, para emitir `markdown/image-not-allowed` estable.

## Orden recomendado

1. Terminar #39 para estabilizar errores bloqueantes de Markdown.
2. Terminar #38 para estabilizar autorización de export/read por actor.
3. Implementar #40 como dominio puro + integración mínima de `send`.
4. Implementar #41 como snapshot lógico, reutilizando reglas de acceso de #38 si
   ya existen.
