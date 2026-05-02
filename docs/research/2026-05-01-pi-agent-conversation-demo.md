# Demo de pi orquestando agentes que usan el chat del producto

Fecha: 2026-05-01

## Objetivo

Mostrar una forma de **dogfooding agentic**: varios agentes pi usan la CLI real
de `pi-clojure` como sala de chat compartida para colaborar, descubrir fricción,
proponer mejoras, crear backlog y luego ejecutar las stories con TDD.

La idea importante es que los agentes no necesitan roles rígidos. Pueden adquirir
roles de forma emergente mientras conversan:

- un agente puede notar un problema de UX;
- otro puede convertirlo en regla de dominio;
- otro puede proponer un test rojo;
- otro puede agrupar hallazgos en issues;
- otro puede implementar una story resultante.

El chat funciona como **blackboard**: una pizarra compartida, append-only y
exportable, donde las contribuciones quedan como evidencia del proceso.

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

## Patrón elegido: Chat-as-Blackboard

En vez de hacer que los agentes conversen en un transcript externo, hacemos que
usen el producto:

```text
pi agent A -> clojure -M:chat send roundtable agent-a "..."
pi agent B -> clojure -M:chat show roundtable agent-b
pi agent B -> clojure -M:chat send roundtable agent-b "..."
pi agent C -> clojure -M:chat export roundtable agent-c --output ...
```

Esto prueba dos cosas a la vez:

1. La capacidad de pi para orquestar agentes.
2. La utilidad y los límites reales del chat Markdown que estamos construyendo.

## Roles emergentes, no asignados

Para evitar conversaciones artificiales, los agentes no deberían tener roles
rígidos. En cambio, cada agente recibe una **personalidad**: un sesgo de mirada
que enriquece la conversación sin impedirle aportar en cualquier dimensión.

Para el primer corte usaremos tres personalidades:

- **Pragmática**: busca el slice más chico ejecutable, reduce alcance y propone
  próximos pasos concretos.
- **Escéptica**: busca bugs, riesgos, casos borde y ambigüedades; cada crítica
  debería venir con un test rojo o una story verificable.
- **Narradora**: cuida claridad, demo, experiencia de uso y cómo la conversación
  se convierte en conocimiento exportable.

Opcionalmente, después podemos sumar:

- **Arquitecta**: cuida invariantes, límites de dominio y acoplamiento.
- **Facilitadora**: resume consenso, detecta duplicados y convierte acuerdos en
  backlog.

El prompt común de cada agente debería decir:

```text
Usá el chat compartido para colaborar. No tenés un rol fijo. Tu personalidad
sesga tu mirada, pero durante la conversación podés actuar como producto,
dominio, UX, seguridad, tester, backlog gardener o implementador según lo que
veas. Leé la sala antes de responder. Si encontrás fricción usando la CLI,
registrala como hallazgo. Si ves una mejora accionable, proponé story con
criterios y primer test rojo.
```

Los roles se vuelven etiquetas dentro de los mensajes, no identidades fijas:

```md
### Hallazgo UX

El error de overwrite no dice cómo recuperarme.

### Propuesta de test rojo

Dado un archivo existente, `export --output` falla y sugiere `--force`.
```

## Flujo de demo mínimo

### 1. Preparar estado aislado

Usar un state file dedicado para la demo:

```sh
export PI_CHAT_STATE_FILE=.demo/agent-roundtable.edn
rm -f "$PI_CHAT_STATE_FILE"
```

### 2. Crear usuarios y sala

```sh
clojure -M:chat create-user pragmatica
clojure -M:chat create-user esceptica
clojure -M:chat create-user narradora
clojure -M:chat create-room roundtable
clojure -M:chat join roundtable pragmatica
clojure -M:chat join roundtable esceptica
clojure -M:chat join roundtable narradora
```

### 3. Lanzar agentes pi en tmux

Cada agente corre en su propio pane/window, con el mismo `PI_CHAT_STATE_FILE` y
un prompt que lo obliga a usar la CLI del producto para hablar:

```text
Antes de responder, ejecutá `clojure -M:chat show roundtable <tu-handle>`.
Para hablar, usá `clojure -M:chat send roundtable <tu-handle> "..." <txn-id>`.
Registrá hallazgos, propuestas, tests rojos y dudas en la sala.
No edites código salvo que una story sea explícitamente asignada.
```

### 4. Conversación colaborativa

Los agentes alternan:

- leer sala;
- probar comandos reales;
- enviar hallazgos;
- proponer mejoras;
- agrupar consenso;
- detectar qué story conviene implementar.

El orquestador puede serializar turnos para evitar escrituras concurrentes sobre
el EDN en la primera versión.

### 5. Exportar evidencia

```sh
clojure -M:chat export roundtable pragmatica \
  --output docs/ideas/agent-roundtable-demo.md \
  --force
```

La exportación se vuelve el artefacto de discovery.

### 6. Crear issues y ejecutar

Un agente backlog gardener lee el export y crea issues concretas:

- historia;
- criterios de aceptación;
- fuera de alcance;
- primer test rojo sugerido;
- riesgo de conflicto.

Luego volvemos al patrón operativo:

- dos implementadores;
- un evaluator/backlog gardener;
- worktrees separados;
- TDD estricto;
- checks y merge fast-forward.

## Fricciones esperadas que la demo puede descubrir

- Estado EDN compartido sin lock para escrituras concurrentes.
- Falta de comandos cómodos para listar historial con filtros.
- Mensajes largos difíciles de escribir por CLI.
- Necesidad de `reply`, `tag`, `decision` o `finding`.
- Exportación útil pero quizá demasiado verbosa para discovery.
- Falta de comando para convertir hallazgos en issues.
- Necesidad de sala de sistema o mensajes de agente con metadata.

## Seguridad y límites

Para el primer corte:

- Los agentes pueden usar `bash` para invocar la CLI, pero se les prohíbe editar
  código durante la fase de conversación.
- El orquestador controla turnos para evitar escrituras simultáneas.
- No se crean issues automáticamente sin confirmación humana.
- Cada mensaje usa `client-txn-id` único y estable.
- El estado de demo vive en `.demo/`, no en datos reales.

## Story sugerida

**Título**: Como mantenedor, quiero una demo dogfood donde agentes usen el chat
por CLI para descubrir backlog.

**Criterios de aceptación**:

- Un script crea un estado aislado en `.demo/agent-roundtable.edn`.
- El script crea al menos tres usuarios agente con personalidades distintas y
  una sala compartida.
- La demo lanza o documenta cómo lanzar agentes pi que leen y escriben en la sala
  usando la CLI real.
- Los agentes no tienen roles rígidos; sus personalidades sesgan su mirada y los
  roles emergen dentro de la conversación.
- La conversación se exporta a Markdown.
- El transcript incluye hallazgos, propuestas de mejora y al menos una story
  candidata con primer test rojo sugerido.
- La demo evita escrituras concurrentes o documenta el riesgo explícitamente.
- No crea issues automáticamente en el primer corte.

**Primer test rojo sugerido**:

Probar una función pura que genere el script de bootstrap o el plan de turnos de
la roundtable. En un segundo paso, probar un script `--dry-run` que produzca un
transcript Markdown fixture sin llamar a modelos.

## Evolución posterior

1. Agregar comando de demo reproducible con `--dry-run` y fixtures.
2. Agregar orquestación RPC para turnos automáticos.
3. Convertir hallazgos del export en borradores de issues con confirmación.
4. Crear extensión pi `/dogfood-roundtable`.
5. Usar el propio chat como memoria de coordinación de agentes durante
   implementación real.
