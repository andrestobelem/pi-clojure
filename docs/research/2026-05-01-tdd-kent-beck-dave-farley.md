# Test Driven Development según Kent Beck y Dave Farley

Fecha: 2026-05-01

## Resumen

Test Driven Development, o TDD, es una disciplina de desarrollo donde las pruebas automatizadas guían el diseño y la implementación del código. Desde la perspectiva de **Kent Beck**, TDD se resume en escribir una prueba que falle, hacerla pasar con la implementación más simple posible y luego refactorizar. Desde la perspectiva de **Dave Farley**, TDD conserva ese ciclo, pero enfatiza especialmente que TDD es una práctica de **diseño de software**, no simplemente una técnica para escribir tests unitarios.

Ambos coinciden en que TDD se basa en feedback rápido, pasos pequeños y diseño incremental.

## Kent Beck: TDD by Example

Kent Beck popularizó y sistematizó TDD en su libro *Test Driven Development: By Example*.

La formulación central de Beck puede resumirse en dos reglas:

1. No escribir código de producción salvo para hacer pasar una prueba automatizada que falla.
2. Eliminar duplicación.

El ciclo clásico es:

```text
Red -> Green -> Refactor
```

### Red

Primero se escribe una prueba pequeña que expresa el próximo comportamiento deseado. Esa prueba debe fallar, porque el comportamiento todavía no existe o todavía no está implementado correctamente.

La prueba fallida ayuda a aclarar el objetivo inmediato.

### Green

Luego se escribe la implementación mínima necesaria para que la prueba pase. En esta fase no se busca el diseño perfecto, sino obtener feedback rápido y avanzar en pasos pequeños.

### Refactor

Una vez que la prueba pasa, se mejora el diseño sin cambiar el comportamiento observable. Las pruebas actúan como red de seguridad para simplificar, eliminar duplicación y mejorar nombres, estructura y responsabilidades.

## Dave Farley: TDD como diseño

Dave Farley, conocido por su trabajo en *Continuous Delivery*, presenta TDD como una práctica central para crear software de alta calidad con feedback rápido.

Farley insiste en que TDD no es simplemente “escribir tests antes”. Para él, TDD es más precisamente **Test Driven Design**: una forma de dejar que las pruebas guíen la estructura del código.

Sus ideas principales:

- TDD ayuda a producir diseños más modulares.
- Las pruebas ejercen presión sobre el diseño: si algo es difícil de testear, probablemente está demasiado acoplado o tiene responsabilidades mezcladas.
- El ciclo `Red -> Green -> Refactor` requiere mentalidades distintas en cada fase.
- TDD funciona mejor con pasos pequeños y feedback muy rápido.
- TDD se conecta naturalmente con Continuous Delivery porque una suite automatizada confiable permite entregar cambios con menor riesgo.

## Las tres mentalidades según Farley

Farley describe cada fase del ciclo como una mentalidad diferente:

### Red: pensar en el problema

En la fase roja, el foco está en entender qué comportamiento se quiere obtener. La prueba expresa una expectativa desde el punto de vista del consumidor del código.

### Green: resolver de la forma más simple

En la fase verde, el foco está en hacer que el sistema funcione. Se permite una solución simple o incluso imperfecta, siempre que permita avanzar y obtener feedback.

### Refactor: mejorar el diseño

En la fase de refactor, el foco cambia hacia la calidad interna: nombres, duplicación, cohesión, acoplamiento, separación de responsabilidades y claridad.

## Puntos en común entre Beck y Farley

- TDD es un ciclo corto de feedback.
- Las pruebas deben escribirse antes del código de producción.
- El diseño emerge incrementalmente mediante refactorización.
- La prueba no es solo verificación posterior: es una herramienta para pensar.
- Los pasos pequeños reducen riesgo y complejidad.
- Una buena suite de tests permite cambiar el código con confianza.

## Diferencias de énfasis

| Tema | Kent Beck | Dave Farley |
| --- | --- | --- |
| Formulación principal | Reglas simples y ciclo Red/Green/Refactor | TDD como disciplina de diseño |
| Foco | Ejemplos pequeños, feedback y eliminación de duplicación | Diseño modular, calidad interna y entrega continua |
| Relación con arquitectura | Diseño incremental desde pruebas pequeñas | Tests como presión para bajo acoplamiento y alta cohesión |
| Relación con delivery | Implícita en feedback y calidad | Explícita: TDD apoya Continuous Delivery |

## Recomendaciones prácticas

1. Escribir primero una prueba pequeña que falle.
2. Hacer pasar la prueba con el cambio más simple posible.
3. Refactorizar solo cuando las pruebas están en verde.
4. Mantener ciclos cortos: minutos, no horas.
5. Si una prueba es difícil de escribir, revisar el diseño.
6. No usar TDD solo como cobertura: usarlo para descubrir interfaces y responsabilidades.
7. Integrar TDD con CI/CD para obtener feedback continuo.

## Relación con commits atómicos

TDD combina muy bien con commits atómicos:

```text
test(parser): describe empty input behavior
fix(parser): handle empty input
refactor(parser): simplify token reader
```

Sin embargo, en muchos equipos también es válido commitear el ciclo completo como un único cambio lógico:

```text
fix(parser): handle empty input
```

incluyendo test, implementación y refactor dentro del mismo commit, siempre que todo pertenezca al mismo comportamiento.

## Fuentes

- [Kent Beck, *Test Driven Development: By Example*, InformIT](https://www.informit.com/store/test-driven-development-by-example-9780321146533)
- [Kent Beck, *Test Driven Development: By Example*, O'Reilly preview](https://www.oreilly.com/library/view/test-driven-development/0321146530/ch27.xhtml)
- [Agile Alliance, “Test Driven Development”](https://agilealliance.org/glossary/tdd/)
- [Dave Farley, “Three Distinct Mind-sets in TDD”](https://www.davefarley.net/?p=260)
- [Dave Farley, “Test Driven Development”](https://www.davefarley.net/?p=220)
- [Continuous Delivery Ltd, About Dave Farley](https://www.continuous-delivery.co.uk/about)
- [CD.Training, “About TDD & BDD”](https://courses.cd.training/pages/about-tdd-bdd)
- [Pearson, *Continuous Delivery* by Jez Humble and Dave Farley](https://www.pearson.com/en-us/subject-catalog/p/continuous-delivery-reliable-software-releases-through-build-test-and-deployment-automation/P200000009113/9780321670229)
