# Política de nombres de usuario

## Pregunta

¿Cuál es el estado del arte para definir handles o nombres de usuario públicos?

## Hallazgos

### Separar identidad interna de handle público

El identificador primario del usuario no debería ser el handle. NIST recomienda
mantener una cuenta única y usar identificadores únicos generados con suficiente
entropía. OWASP también recomienda que los user ids identifiquen unívocamente al
usuario y que idealmente sean aleatorios.

Decisión sugerida: usar en el futuro un `:user/id` opaco e inmutable, y tratar
`:user/handle` como identificador público mutable o al menos no como clave
primaria global del dominio.

### Canonicalizar y validar en servidor

Para handles públicos simples, el patrón más robusto es:

- normalizar antes de validar y persistir;
- guardar un `handle_canonical` o equivalente;
- aplicar índice único sobre el valor canonicalizado;
- no depender de collation específica de la base de datos;
- comparar por el valor canonicalizado.

Para nuestro slice actual, `trim` + `lower-case` + índice por handle normalizado
va en esa dirección.

### Preferir ASCII para handles nuevos

Para un producto inicial, el enfoque más seguro y simple es restringir handles a
ASCII URL-safe y dejar Unicode expresivo para un campo separado de display name.

Una política común es permitir:

- letras minúsculas `a-z`;
- números `0-9`;
- separadores limitados como `-` y/o `_`;
- longitud mínima y máxima explícita;
- sin separador al inicio o final;
- sin separadores repetidos, si queremos URLs y lectura más limpias.

GitHub, por ejemplo, limita usernames a 39 caracteres, permite caracteres
alfanuméricos y guiones, y rechaza guiones al inicio/final, guiones consecutivos
y colisiones tras normalización.

### Unicode requiere una especificación, no una regex casera

Si más adelante queremos handles Unicode, conviene adoptar una especificación
existente:

- PRECIS `UsernameCaseMapped` / `IdentifierClass`;
- Unicode UTS #39 para seguridad de identificadores.

Eso implica manejar normalización NFC, case mapping, bidireccionalidad,
caracteres invisibles, caracteres no asignados, confusables y mezcla de scripts.
No es recomendable improvisarlo con una regex propia.

### Evitar impersonation y nombres reservados

Además de la unicidad técnica, hay que considerar abuso e impersonation:

- reservar handles del sistema: `admin`, `root`, `support`, `api`, `www`, etc.;
- detectar o bloquear confusables si se permite Unicode;
- evitar nombres que solo difieran por mayúsculas, espacios o caracteres
  invisibles;
- auditar cambios de handle si en el futuro son editables.

### Evitar enumeración de usuarios en flujos sensibles

OWASP recomienda respuestas genéricas en login, recuperación y registro cuando
aplique, para no filtrar si un usuario existe. En nuestro dominio puede ser
válido devolver `handle already exists` en creación, pero la capa pública deberá
decidir si muestra ese detalle o una respuesta genérica según el contexto.

## Recomendación para este repo ahora

Mantener el slice simple con handles ASCII canonicalizados:

- canonicalización: `trim` + `lower-case`;
- caracteres: `a-z`, `0-9`, `-`, `_`;
- unicidad sobre el handle canonicalizado;
- agregar longitud mínima y máxima;
- rechazar separador inicial/final;
- rechazar separadores consecutivos si queremos una política más estricta;
- documentar que el store actual es in-memory y que en persistencia real habrá
  un índice único sobre el canonical handle;
- crear más adelante `:user/id` opaco para no usar `:user/handle` como identidad
  interna permanente.

## Fuentes

- NIST SP 800-63A-4, Subscriber Accounts:
  <https://pages.nist.gov/800-63-4/sp800-63a/accounts/>
- OWASP Authentication Cheat Sheet:
  <https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html>
- RFC 8265, PRECIS: Usernames and Passwords:
  <https://www.ietf.org/rfc/rfc8265.html>
- RFC 8264, PRECIS Framework:
  <https://www.rfc-editor.org/rfc/rfc8264.html>
- Unicode UTS #39, Unicode Security Mechanisms:
  <https://unicode.org/reports/tr39/>
- GitHub Enterprise Server Docs, Username considerations:
  <https://docs.github.com/en/enterprise-server@3.17/admin/managing-iam/iam-configuration-reference/username-considerations-for-external-authentication>
