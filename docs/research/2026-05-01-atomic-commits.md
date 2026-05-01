# Commits atómicos

Fecha: 2026-05-01

## Resumen

Un commit atómico es un commit que representa un único cambio lógico: una funcionalidad pequeña, una corrección, un refactor, una migración o una actualización de documentación concreta. La idea no es hacer commits microscópicos por cada línea modificada, sino cambios coherentes, revisables y fáciles de revertir.

En Git, un buen commit atómico debería ser:

- **Un cambio lógico único**: no mezclar fixes, features, refactors y formateos no relacionados.
- **Coherente**: todas las modificaciones dentro del commit tienen una misma intención.
- **Revisable**: suficientemente pequeño para entenderlo en code review.
- **Reversible**: se puede revertir sin deshacer trabajo no relacionado.
- **Idealmente funcional/testeable**: el repo debería quedar en un estado consistente después del commit.

## Beneficios

- **Reviews más simples**: cada commit cuenta una parte clara de la historia.
- **Reverts más seguros**: si algo falla, se revierte solo el cambio problemático.
- **Mejor `git bisect`**: facilita encontrar el commit exacto que introdujo un bug.
- **Mejor `git blame`**: ayuda a entender por qué se cambió una línea.
- **Cherry-picks más limpios**: permite mover fixes o cambios concretos entre ramas.
- **Historial más útil**: los commits documentan decisiones pequeñas y concretas.

## Buenas prácticas

1. **Separar cambios por intención**

   Ejemplos de commits separados:

   ```text
   refactor(parser): extract token reader
   fix(parser): handle empty input
   docs(parser): document empty input behavior
   ```

   Evitar mezclar todo en un solo commit como:

   ```text
   fix parser and update docs and cleanup formatting
   ```

2. **Usar staging parcial**

   Cuando el working tree contiene cambios mezclados, usar:

   ```sh
   git add -p
   git commit -p
   git restore -p
   ```

   Esto permite seleccionar hunks específicos y construir commits más enfocados.

3. **Limpiar historia antes de compartirla**

   Para commits locales todavía no publicados, se puede usar:

   ```sh
   git commit --amend
   git commit --fixup <commit>
   git rebase -i --autosquash
   ```

   Importante: evitar reescribir historia que ya fue compartida, salvo acuerdo del equipo.

4. **Mantener cada commit en estado consistente**

   Siempre que sea posible, cada commit debería compilar y pasar tests relevantes. Esto vuelve más útil `git bisect` y reduce sorpresas al revisar.

5. **No confundir atómico con demasiado pequeño**

   Un commit atómico puede tocar varios archivos si todos forman parte del mismo cambio lógico. Por ejemplo, cambiar una función, sus tests y su documentación puede ser un único commit correcto.

## Relación con Conventional Commits

Los commits atómicos combinan bien con Conventional Commits porque el tipo y scope ayudan a expresar la intención de cada cambio:

```text
feat(repl): add command history
fix(reader): handle nil forms
refactor(core): split evaluator helpers
test(reader): cover invalid tokens
docs: add commit guidelines
```

Si un commit necesita varios tipos distintos para describirse, probablemente no es atómico y conviene dividirlo.

## Recomendaciones para este repo

1. Hacer commits pequeños y enfocados en un único objetivo.
2. Usar Conventional Commits para describir la intención.
3. Separar cambios de documentación, refactor, fixes y features cuando no dependan directamente entre sí.
4. Usar `git add -p` antes de commitear si hay cambios mezclados.
5. Antes de abrir un PR o compartir una rama, revisar la historia con:

   ```sh
   git log --oneline --decorate
   git show --stat <commit>
   ```

6. Si la rama todavía es local, limpiar commits ruidosos con interactive rebase.

## Fuentes

- [Git `gitworkflows`](https://git-scm.com/docs/gitworkflows)
- [Pro Git, "Contributing to a Project"](https://git-scm.com/book/en/v2/Distributed-Git-Contributing-to-a-Project)
- [Git `git commit`](https://git-scm.com/docs/git-commit)
- [GitHub Docs, "Options for managing commits in GitHub Desktop"](https://docs.github.com/en/desktop/managing-commits/options-for-managing-commits-in-github-desktop)
- [GitLab, "What are Git version control best practices?"](https://about.gitlab.com/topics/version-control/version-control-best-practices/)
