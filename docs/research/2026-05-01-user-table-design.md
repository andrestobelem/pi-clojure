# Diseño de tabla de usuarios

## Contexto

La story #23 diseña la persistencia inicial de usuarios para Dolt, alineada con
el dominio actual:

```clojure
#:user{:handle "andres"
       :type :user.type/human}
```

El endurecimiento de handles quedó definido en #22 y documentado en
[`2026-05-01-username-policy.md`](2026-05-01-username-policy.md).

## Esquema propuesto

El esquema inicial está en [`../../db/dolt/001_chat_markdown.sql`](../../db/dolt/001_chat_markdown.sql):

```sql
CREATE TABLE IF NOT EXISTS users (
  id VARCHAR(36) NOT NULL,
  handle VARCHAR(39) NOT NULL,
  user_type VARCHAR(32) NOT NULL,
  display_name VARCHAR(120),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY users_handle_unique (handle),
  KEY users_type_idx (user_type)
);
```

## Decisiones

### Identificador interno separado del handle

`id` es el identificador interno estable y no depende del handle. Para el MVP se
representa como `VARCHAR(36)` para aceptar UUIDs canónicos.

El handle público no debe usarse como clave primaria, porque en el futuro podría
ser mutable o requerir reglas de producto distintas a la identidad interna.

### Handle canónico con unicidad

`handle` guarda el handle ya canónico y validado por el dominio. La tabla aplica
`UNIQUE KEY users_handle_unique (handle)` para proteger la unicidad en
persistencia.

No agregamos una columna separada `handle_canonical` porque la política actual
rechaza handles no canónicos en vez de normalizarlos silenciosamente. Por eso el
valor persistido en `handle` ya es el valor canónico.

### Tipo de usuario explícito

`user_type` conserva el tipo de usuario. El mapeo actual es:

| Dominio | Tabla |
| --- | --- |
| `:user.type/human` | `human` |
| `:user.type/agent` | `agent` |

No agregamos todavía una restricción SQL para los valores permitidos: la regla
vive en el dominio mediante `:user/type` y `user-types`. Podemos sumar una
restricción en una migración posterior si Dolt/MySQL compatibility lo requiere.

### Display name opcional

`display_name` queda opcional. El dominio actual no lo usa, pero la columna deja
espacio para mostrar un nombre expresivo distinto del handle sin mezclarlo con el
identificador público URL-safe.

### Timestamps

`created_at` y `updated_at` existen desde el diseño inicial. `updated_at` no usa
`ON UPDATE` todavía para mantener el esquema simple y dejar que la capa de
persistencia futura decida cómo actualizarlo.

### Validación del handle

No duplicamos toda la política de #22 como constraints SQL. El dominio conserva
las reglas completas:

- longitud 3..39;
- lowercase;
- sin espacios alrededor;
- caracteres `a-z`, `0-9`, `-`, `_`;
- sin separadores al inicio/final;
- sin separadores consecutivos;
- nombres reservados.

La base refuerza lo crítico para consistencia de datos:

- `handle NOT NULL`;
- `handle VARCHAR(39)`;
- `UNIQUE KEY users_handle_unique (handle)`.

## Mapeo dominio-tabla

| Clojure | SQL |
| --- | --- |
| `:user/id` futuro | `users.id` |
| `:user/handle` | `users.handle` |
| `:user/type` | `users.user_type` |

El dominio actual todavía no tiene `:user/id`; eso queda fuera de esta story y
debería entrar en una story separada antes de implementar persistencia real desde
Clojure.

## Fuera de alcance

- Implementar JDBC desde Clojure.
- Migrar datos existentes.
- Login/autenticación.
- Cambios de handle.
- Tabla separada de nombres reservados.
