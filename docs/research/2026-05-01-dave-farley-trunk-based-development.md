# Dave Farley sobre trunk-based development

Fecha: 2026-05-01

## Resumen

Dave Farley defiende con fuerza **trunk-based development** como una práctica
central de Continuous Integration y Continuous Delivery.

Para Farley, integración continua no significa simplemente tener un servidor de
CI. Significa que las personas integran cambios pequeños en `main`/`trunk` al
menos una vez por día, idealmente varias veces por día, con feedback automatizado
rápido y confiable.

Su postura puede resumirse así:

- CI real requiere integrar frecuentemente en trunk.
- Las ramas largas retrasan feedback y esconden problemas.
- GitFlow y feature branches largas son incompatibles con CI/CD efectivo.
- Si se usan ramas, deben ser muy cortas y volver rápido a trunk.
- El trabajo debe dividirse en cambios pequeños y seguros.
- La mainline debe mantenerse verde y potencialmente desplegable.

## Qué entiende Farley por integración continua

Farley insiste en que CI no es una herramienta, sino una práctica.

No alcanza con tener Jenkins, GitHub Actions, GitLab CI o cualquier pipeline si
el equipo trabaja durante días o semanas en ramas separadas. En ese caso, el
sistema de CI valida ramas aisladas, pero no valida continuamente el estado real
del producto integrado.

Para que sea CI de verdad:

1. Los cambios se integran en trunk al menos diariamente.
2. Cada integración dispara tests automatizados.
3. El equipo mantiene la build verde.
4. Los cambios son pequeños.
5. El feedback llega rápido.
6. La rama principal representa el estado real del producto.

## Crítica a feature branches largas

Farley critica las ramas largas porque posponen la integración.

Una feature branch larga parece segura porque aísla el trabajo, pero ese
aislamiento es precisamente el problema: el feedback sobre integración llega
tarde.

Problemas típicos:

- conflictos de merge más grandes;
- bugs descubiertos tarde;
- reviews enormes;
- cambios difíciles de revertir;
- menor visibilidad del estado real del sistema;
- menor frecuencia de despliegue;
- más coordinación manual;
- más riesgo al final del ciclo.

Desde esta mirada, una rama larga no reduce el riesgo: lo acumula.

## Crítica a GitFlow

Farley suele ser crítico con modelos como GitFlow cuando se usan para separar
trabajo durante largos periodos.

GitFlow puede ser útil en algunos contextos de release management tradicional,
pero choca con Continuous Delivery si promueve:

- ramas de desarrollo de larga vida;
- ramas de release separadas;
- integración tardía;
- estabilización al final;
- grandes lotes de cambios.

Para Farley, CD necesita que el software esté siempre cerca de un estado
releasable. Eso se logra mejor con trunk-based development y automatización, no
con ramas largas de estabilización.

## ¿Farley prohíbe todas las ramas?

No necesariamente. La distinción importante es la duración y el rol de la rama.

Farley acepta que algunas ramas pueden existir si son muy cortas. Por ejemplo,
una rama creada desde trunk para un PR pequeño puede ser compatible con el
modelo si:

- dura horas, no días o semanas;
- vuelve a trunk el mismo día o muy rápido;
- contiene un cambio pequeño;
- tiene tests automatizados;
- se elimina después del merge;
- no se usa para acumular una feature grande.

Como regla práctica: si una rama vive más de un día, ya empieza a alejarse del
espíritu de CI. Algunas guías aceptan hasta dos días como transición, pero la
dirección deseada es integrar más frecuentemente.

## Cómo trabajar sin ramas largas

Farley propone reducir el tamaño de los cambios y ocultar trabajo incompleto sin
separarlo del trunk durante mucho tiempo.

Técnicas frecuentes:

### Cambios pequeños

Dividir el trabajo en pasos seguros y revisables.

En vez de una rama de dos semanas para una feature, integrar varias piezas
pequeñas que mantengan el sistema funcionando.

### Branch by abstraction

Crear una abstracción que permita cambiar una implementación gradualmente.

Esto permite integrar refactors grandes en pasos pequeños sin romper el sistema.

### Feature flags

Integrar código incompleto o no habilitado detrás de flags.

La feature puede estar en trunk sin estar disponible para usuarios finales.

### Dark launching

Desplegar código en producción sin exponerlo todavía o sin activar todo el
comportamiento para usuarios.

### TDD y tests automatizados

Los tests rápidos permiten integrar con confianza. Sin una buena suite de tests,
trunk-based development se vuelve riesgoso.

## Relación con Continuous Delivery

Para Farley, trunk-based development es una base de Continuous Delivery.

CD busca que cada cambio integrado pueda avanzar por una pipeline automatizada y
estar potencialmente listo para producción. Eso requiere que la rama principal
sea el lugar donde ocurre la integración real.

Si el equipo integra tarde, entonces la pipeline no está validando el producto
integrado de manera continua.

En otras palabras:

```text
trunk-based development -> continuous integration real -> continuous delivery
```

## Relación con pull requests

La postura de Farley puede incomodar en equipos que dependen de pull requests
largos como mecanismo principal de control.

Una lectura compatible con trunk-based development sería:

- PRs pequeños;
- vida corta;
- review rápida;
- automatización fuerte;
- merge frecuente;
- preferencia por pairing o ensemble programming cuando se necesita feedback
  antes de integrar.

El problema no es el PR en sí, sino usar PRs como cola de integración durante
días o semanas.

## Relación con commits atómicos

Trunk-based development necesita commits pequeños y seguros.

Esto encaja con la política de commits atómicos:

```text
fix(parser): handle empty input
refactor(reader): extract token stream
feat(repl): add history flag
```

Cada commit debería tener una intención clara, ser revisable y dejar el repo en
un estado consistente.

## Relación con TDD y Test Driven Design

Farley conecta trunk-based development con TDD porque ambas prácticas reducen el
riesgo mediante feedback rápido.

- TDD da feedback de diseño y comportamiento a nivel de código.
- CI da feedback de integración.
- CD da feedback de entrega.

Si los cambios son pequeños, testeados y se integran frecuentemente, el sistema
puede evolucionar sin grandes fases de integración o estabilización.

## Recomendaciones para este repo

1. Trabajar contra `main` como rama principal de integración.
2. Evitar ramas de larga vida.
3. Si usamos ramas, que sean pequeñas y duren poco.
4. Integrar cambios al menos diariamente cuando haya desarrollo activo.
5. Mantener commits atómicos y convencionales.
6. Mantener `main` en estado consistente.
7. Agregar tests automatizados antes de aumentar la velocidad de integración.
8. Usar feature flags o branch by abstraction para cambios grandes.
9. Preferir cambios incrementales sobre grandes merges.

## Fuentes

- [Dave Farley, “Continuous Integration and Feature Branching”](https://www.davefarley.net/?p=247)
- [Dave Farley, “How To - Continuous Integration”](https://continuous-delivery.co.uk/downloads/How%20To%20-%20Continuous%20Integration%202.pdf)
- [Dave Farley, “The State of Continuous Delivery in 2025”](https://continuous-delivery.co.uk/cd-assessment/index)
- [ContinuousDelivery.com, “Continuous Integration”](https://continuousdelivery.com/foundations/continuous-integration/)
- [ContinuousDelivery.com, “On DVCS, continuous integration, and feature branches”](https://continuousdelivery.com/2011/07/on-dvcs-continuous-integration-and-feature-branches/)
- [DORA, “Trunk-based development”](https://dora.dev/capabilities/trunk-based-development/)
- [DORA, “Continuous delivery”](https://dora.dev/capabilities/continuous-delivery/)
- [MinimumCD](https://minimumcd.org/)
- [TrunkBasedDevelopment.com, “Introduction”](https://trunkbaseddevelopment.com/)
- [TrunkBasedDevelopment.com, “Short-Lived Feature Branches”](https://trunkbaseddevelopment.com/short-lived-feature-branches/)
