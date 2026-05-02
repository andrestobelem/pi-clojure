# Demo end-to-end: chat Markdown exportable

Este guion reproduce el flujo MVP de chat exportable en un estado temporal y
aislado. No lee ni modifica `.pi-chat.edn` del checkout.

## Ejecutar

Desde la raíz del repo:

```sh
scripts/demo-export-chat.sh
```

Para conservar los artefactos en un directorio conocido:

```sh
DEMO_TMP_DIR=/tmp/pi-clojure-demo scripts/demo-export-chat.sh
```

El script usa `set -euo pipefail`, borra solo `state.edn` y
`general-export.md` dentro de `DEMO_TMP_DIR`, y falla con exit code distinto de
cero si un comando falla o si el Markdown exportado no contiene los fragmentos
esperados.

## Flujo cubierto

El guion ejecuta comandos copiables de la CLI para:

1. crear dos usuarios humanos (`andres` y `zoe`);
2. demostrar que cada usuario tiene sala personal;
3. crear una sala compartida (`general`);
4. unir ambos usuarios a la sala compartida;
5. enviar mensajes Markdown válidos;
6. mostrar la conversación ordenada;
7. exportar la sala a `general-export.md`;
8. verificar que el archivo exportado existe y contiene los mensajes esperados.

## Salida representativa

Las rutas temporales varían por ejecución. La forma relevante de la salida es:

```text
Demo end-to-end de chat Markdown exportable
Estado aislado: /tmp/pi-clojure-demo/state.edn

$ clojure -M:chat create-user andres
Usuario creado: andres
Sala personal de andres disponible: room:user:andres

$ clojure -M:chat create-user zoe
Usuario creado: zoe
Sala personal de zoe disponible: room:user:zoe

$ clojure -M:chat create-room general
Sala creada: general

$ clojure -M:chat join general andres
andres entró a general

$ clojure -M:chat join general zoe
zoe entró a general

$ clojure -M:chat send general andres 'Hola **equipo**' demo-andres-1
Mensaje enviado a general por andres

$ clojure -M:chat send general zoe '- item uno
- item dos' demo-zoe-1
Mensaje enviado a general por zoe

$ clojure -M:chat show general andres
# General

## Mensajes

1. andres: Hola **equipo**
2. zoe: - item uno
- item dos

$ clojure -M:chat export general andres --output /tmp/pi-clojure-demo/general-export.md
Exportación escrita en /tmp/pi-clojure-demo/general-export.md para la sala general con 2 mensajes

Exportación verificada: /tmp/pi-clojure-demo/general-export.md

Primeras líneas del Markdown exportado:
# General

## Mensajes

### Mensaje 1

Hola **equipo**
```
