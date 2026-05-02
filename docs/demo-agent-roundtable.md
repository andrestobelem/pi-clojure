# Demo dogfood: agentes usando el chat

Esta demo muestra agentes pi colaborando a través del chat real de `pi-clojure`.
El chat funciona como pizarra compartida: los agentes leen la sala, escriben
hallazgos con la CLI y exportan la conversación como evidencia de discovery.

## Ejecutar demo determinística

```sh
scripts/demo-agent-roundtable.sh
```

La demo usa estado aislado en `.demo/agent-roundtable.edn`, exporta el
transcript en `.demo/agent-roundtable-demo.md` y genera una auditoría en
`.demo/agent-roundtable-audit.md`.

Para correrla en un directorio temporal:

```sh
DEMO_TMP_DIR="$(mktemp -d)" scripts/demo-agent-roundtable.sh
```

## Personalidades

La demo crea tres usuarios agente. No son roles fijos; cada personalidad sesga la
mirada y los roles emergen durante la conversación.

- `pragmatica`: busca el slice mínimo ejecutable y próximos pasos concretos.
- `esceptica`: busca bugs, riesgos, edge cases y tests rojos.
- `narradora`: cuida claridad, demo y experiencia de uso.

## Lanzar agentes pi manualmente

El script genera prompts en `.demo/prompts/`. Con la variable
`PI_CHAT_STATE_FILE` apuntando al mismo estado, se pueden abrir tres sesiones pi:

```sh
PI_CHAT_STATE_FILE=.demo/agent-roundtable.edn pi "$(cat .demo/prompts/pragmatica.md)"
PI_CHAT_STATE_FILE=.demo/agent-roundtable.edn pi "$(cat .demo/prompts/esceptica.md)"
PI_CHAT_STATE_FILE=.demo/agent-roundtable.edn pi "$(cat .demo/prompts/narradora.md)"
```

Cada agente debe usar la CLI real para leer y escribir:

```sh
clojure -M:chat show roundtable pragmatica
clojure -M:chat send roundtable pragmatica "<mensaje Markdown>" "<client-txn-id>"
```

## Límite conocido

El estado EDN de la CLI no tiene locking. Para el primer corte, la demo
serializa turnos y evita escrituras concurrentes. Si usamos agentes simultáneos,
conviene coordinar turnos o convertir esa fricción en una story de locking.

## Salida esperada

El transcript exportado debería incluir:

- hallazgos surgidos usando la CLI real;
- propuestas de mejora;
- una story candidata;
- primer test rojo sugerido.

La auditoría debería incluir:

- estado aislado usado;
- sala y participantes;
- comandos CLI ejecutados;
- límite de turnos seriales;
- fricciones observadas.
