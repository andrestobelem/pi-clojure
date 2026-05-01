# Usuarios humanos y agentes

El sistema debería modelar usuarios humanos y usuarios agentes con las mismas
capacidades y prohibiciones de base.

La diferencia entre humano y agente no debería estar en permisos especiales
implícitos, sino en su tipo de identidad, forma de autenticación, capacidades
declaradas y reglas operativas configuradas por sala u organización.

## Principio

Un agente es un usuario del sistema.

Por defecto, si un usuario humano puede realizar una acción, un usuario agente
podría realizarla también, siempre que tenga los mismos permisos explícitos. Del
mismo modo, si una acción está prohibida para un humano, también está prohibida
para un agente.

```text
misma acción
  -> misma autorización
  -> misma auditoría
  -> mismas restricciones
```

## Implicaciones

Los humanos y agentes pueden compartir el mismo modelo para:

- pertenecer a salas;
- enviar mensajes Markdown;
- editar o borrar mensajes según permisos;
- recibir menciones;
- participar en salas personales, directas o compartidas;
- crear documentos o snapshots si tienen permiso;
- ser moderados;
- ser bloqueados o expulsados;
- aparecer en auditoría e historial.

## Diferencias explícitas

Aunque compartan capacidades y prohibiciones, conviene distinguir su naturaleza:

```clojure
{:user/type :user.type/human}
{:user/type :user.type/agent}
```

Un usuario agente puede declarar capacidades operativas:

```clojure
{:agent/capabilities #{:summarize :lint :research :moderate :export}}
```

Estas capacidades no reemplazan permisos. Solo describen qué sabe hacer el
agente. Para actuar, igualmente necesita autorización.

## Auditoría

Toda acción debe quedar atribuida al usuario que la ejecutó, sea humano o
agente.

```clojure
{:message/id "msg_123"
 :message/author-id "agent_summary"
 :message/author-type :user.type/agent
 :message/body-markdown "## Resumen\n..."
 :message/created-at instant}
```

Si un agente actúa por encargo de un humano, debería registrarse también el
usuario solicitante:

```clojure
{:event/type :message/created
 :event/actor-id "agent_summary"
 :event/requested-by "user_123"}
```

## Seguridad y confianza

Los agentes no deberían tener privilegios mágicos por ser agentes. Necesitan:

- identidad propia;
- permisos explícitos;
- límites de rate limit;
- límites de contexto;
- políticas de sala;
- auditoría;
- posibilidad de revocación.

## Preguntas abiertas

- ¿Un agente puede tener sala personal propia?
- ¿Un humano puede instalar agentes privados en su sala personal?
- ¿Los agentes pueden iniciar conversaciones o solo responder?
- ¿Cómo se muestra visualmente que un usuario es agente?
- ¿Cómo se audita el contexto usado por un agente?
- ¿Los agentes pueden actuar en nombre de un humano o siempre como sí mismos?
