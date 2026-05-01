# Visión del producto

Queremos construir un chat Markdown-first para convertir conversaciones en
conocimiento versionado.

A diferencia de un chat tradicional, donde las conversaciones se pierden en un
flujo efímero, cada sala debería funcionar como conversación en tiempo real y
también como documento vivo. Los mensajes se escriben en Markdown validado,
seguro y linteable; pueden versionarse, auditarse, resumirse, exportarse y
eventualmente transformarse en decisiones, notas, documentación o propuestas.

La hipótesis técnica inicial es explorar persistencia tipo Git, empezando con
Dolt, para habilitar diffs, commits, snapshots y eventualmente ramas o forks de
conversaciones.

Además de salas compartidas, cada usuario debería tener una sala personal privada
donde pueda capturar ideas, conversar consigo mismo o con agentes, y preparar
conocimiento antes de compartirlo. Los usuarios pueden ser humanos o agentes; en
ambos casos deben compartir las mismas capacidades y prohibiciones de base,
diferenciándose por identidad, capacidades declaradas y permisos explícitos.

## Problema

El problema principal no es mandar mensajes. El problema es que las
conversaciones importantes se pierden:

- las decisiones quedan enterradas;
- cuesta reconstruir contexto;
- pasar de conversación a documento es trabajo manual;
- los chats tradicionales no son buenos artefactos de conocimiento;
- falta trazabilidad entre idea, discusión, decisión y documento final.

## Promesa

Conversar como en un chat, conservar como en Git y publicar como Markdown.

## Primer usuario ideal

Equipos técnicos, comunidades open source, grupos de estudio o personas que ya
piensan y trabajan con Markdown, Git y documentación versionada.

## Momento mágico

Después de una conversación larga, la sala puede convertirse en un documento
Markdown limpio, con contexto, decisiones, enlaces y citas trazables al historial
original.

Otro momento importante sería ver un diff entre snapshots para entender cómo una
idea evolucionó desde una conversación inicial hasta una decisión final.

## No objetivos iniciales

No queremos empezar como:

- reemplazo genérico de Slack, Discord o WhatsApp;
- red social;
- wiki tradicional;
- editor colaborativo genérico;
- sistema de mensajería sin memoria durable.

## MVP que expresa la visión

Para probar la tesis, el MVP debería incluir:

- salas de chat;
- mensajes Markdown;
- historial persistente;
- exportación de sala como Markdown;
- snapshots o commits de conversación;
- diff entre snapshots;
- marcas simples como decisión, nota o pregunta.
