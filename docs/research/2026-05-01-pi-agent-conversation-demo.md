# Demo de pi orquestando agentes que conversan entre sí

Fecha: 2026-05-01

## Objetivo

Mostrar que pi no solo sirve como agente individual, sino como harness para
orquestar varios agentes con roles distintos que conversan entre sí para llenar
backlog, refinar stories y preparar implementación.

La demo debería ser comprensible para una persona mirando la terminal:

```text
moderador -> plantea objetivo
producto  -> propone valor de usuario
dominio   -> detecta reglas y riesgos
ux        -> detecta feedback, seguridad y demo
moderador -> sintetiza issues y siguiente tanda
```

## Capacidades de pi relevantes

Según la documentación de pi:

- pi no trae sub-agentes built-in; recomienda usar `tmux`, extensiones o SDK.
- `tmux` da observabilidad total y control humano directo.
- RPC mode (`pi --mode rpc`) permite controlar agentes headless con JSONL por
  stdin/stdout.
- SDK (`createAgentSession`) permite crear sesiones programáticas dentro de una
  app Node/TypeScript.
- Las extensiones pueden registrar comandos, herramientas, interceptar eventos y
  enviar mensajes con `pi.sendUserMessage` o `pi.sendMessage`.
- Las extensiones pueden usar un event bus compartido o archivos para coordinar
  con sistemas externos.

## Tres niveles de demo posibles

### Nivel 1: tmux como orquestador humano

Es el patrón que ya usamos:

- Crear worktrees separados.
- Abrir una ventana `tmux` por agente.
- Dar roles diferentes: producto, dominio, UX, evaluator.
- El humano copia/sintetiza salidas o un coordinador las lee desde archivos.

Ventajas:

- Muy simple.
- No requiere código nuevo.
- Ideal para enseñar el patrón operativo real.

Limitaciones:

- Los agentes no se hablan directamente; el humano coordina.
- Difícil reproducir una conversación cerrada como demo automática.

### Nivel 2: orquestador RPC externo

Crear un script, por ejemplo `scripts/pi-roundtable-demo.mjs`, que:

1. Spawnea varios procesos:

   ```sh
   pi --mode rpc --no-session --tools read,grep,find,ls \
     --system-prompt "Sos agente Producto..."
   ```

2. Envía un prompt inicial a cada agente.
3. Captura texto por eventos `message_update` hasta `agent_end`.
4. Agrega cada respuesta a un transcript compartido.
5. Pasa el transcript al siguiente agente.
6. Pide a un moderador una síntesis final.

Ventajas:

- Demo reproducible.
- Los agentes conversan de verdad mediante transcript.
- Se puede limitar a herramientas read-only para seguridad.
- Funciona fuera de pi interactivo y puede grabarse fácilmente.

Limitaciones:

- Hay que manejar procesos, JSONL y errores.
- Consume más tokens porque replica transcript en varias sesiones.

### Nivel 3: extensión `/roundtable` dentro de pi

Crear una extensión project-local `.pi/extensions/roundtable.ts` con un comando:

```text
/roundtable "Objetivo: proponer próximas stories"
```

La extensión podría:

- Crear sesiones SDK en memoria para roles `producto`, `dominio`, `ux` y
  `moderador`.
- Mostrar estado en la UI de pi con `ctx.ui.setWidget`.
- Guardar transcript en `docs/ideas/roundtable-YYYY-MM-DD.md`.
- Opcionalmente crear issues con confirmación humana.

Ventajas:

- Demo integrada en pi.
- Se ve como una feature propia del harness.
- Puede evolucionar a herramienta diaria de backlog gardening.

Limitaciones:

- Más compleja.
- Las extensiones corren con permisos completos; hay que mantener controles.
- Conviene empezar con RPC/script antes de convertirlo en extensión.

## Recomendación

Para una demo próxima, usar **Nivel 2: orquestador RPC externo**.

Razones:

- Es suficientemente automático para demostrar conversación entre agentes.
- No requiere meterse todavía en UI de extensiones.
- Se puede versionar como script reproducible.
- Permite ejecutar agentes con herramientas read-only.
- Encaja con nuestro patrón: discovery/refinement antes de implementación.

Luego, si la demo resulta útil, convertirla en extensión `/roundtable`.

## Diseño de la demo RPC

### Roles

- `moderador`: define agenda, hace preguntas y sintetiza.
- `producto`: busca valor, demo y slicing de usuario.
- `dominio`: busca reglas, invariantes, riesgos de datos y TDD.
- `ux-seguridad`: busca feedback CLI, errores, seguridad Markdown y edge cases.

### Flujo mínimo

```text
turno 0: moderador presenta objetivo
turno 1: producto responde
turno 2: dominio responde viendo producto
turno 3: ux-seguridad responde viendo producto + dominio
turno 4: moderador sintetiza 3-5 issues candidatas
```

### Contrato de salida

El moderador final debe producir Markdown con:

- Decisiones principales.
- Issues candidatas con historia, criterios y primer test rojo sugerido.
- Riesgos de conflicto por archivo.
- Recomendación de dos implementadores + un evaluator.

### Seguridad

Para la primera demo:

- Usar `--tools read,grep,find,ls` o `--no-tools`.
- No permitir `bash`, `edit` ni `write` a los agentes participantes.
- El script escribe el transcript, no los agentes.
- No crear issues automáticamente; solo proponer.

## Sketch técnico RPC

```js
import { spawn } from "node:child_process";

function startAgent(name, systemPrompt) {
  const proc = spawn("pi", [
    "--mode", "rpc",
    "--no-session",
    "--tools", "read,grep,find,ls",
    "--system-prompt", systemPrompt,
  ]);

  return {
    name,
    prompt(message) {
      proc.stdin.write(JSON.stringify({ type: "prompt", message }) + "\n");
      return collectUntilAgentEnd(proc.stdout);
    },
    stop() {
      proc.kill();
    },
  };
}
```

Puntos importantes del protocolo:

- Leer stdout como JSONL separado estrictamente por `\n`.
- Acumular `message_update` con `assistantMessageEvent.type == "text_delta"`.
- Considerar terminada una respuesta cuando llega `agent_end`.
- Correlacionar errores con eventos y respuestas `success: false`.

## Story sugerida

**Título**: Como mantenedor, quiero una demo roundtable de agentes pi para
proponer backlog colaborativamente.

**Criterios de aceptación**:

- `scripts/pi-roundtable-demo.mjs` ejecuta una conversación entre al menos tres
  roles y un moderador.
- La demo acepta un objetivo por argumento.
- La salida se guarda como Markdown en `docs/ideas/roundtable-demo.md` o una ruta
  indicada.
- Los agentes participantes corren sin herramientas de escritura.
- El transcript final incluye propuestas de issues con criterios y primer test
  rojo sugerido.
- El script falla con mensaje claro si `pi` no está disponible o si un agente no
  responde.

**Primer test rojo sugerido**:

Probar una función pura de formateo de transcript que recibe turnos
`[{role, content}]` y produce Markdown estable con secciones por rol. Luego
integrar el runner RPC detrás de una frontera testeable.

## Evolución posterior

1. Convertir el script en comando `bb`/npm si se usa mucho.
2. Agregar `--dry-run` con respuestas fixture para demos sin gastar tokens.
3. Crear extensión `/roundtable` que use SDK o RPC y muestre progreso dentro de
   pi.
4. Agregar confirmación humana para convertir propuestas en GitHub Issues.
