# Test Driven Design

Fecha: 2026-05-01

## Resumen

**Test Driven Design** es una forma de entender TDD donde el foco principal no es “tener tests”, sino **usar tests para descubrir y mejorar el diseño del software**.

La idea central es que escribir una prueba antes del código obliga a diseñar desde el punto de vista del usuario del código: API, responsabilidades, dependencias, nombres, efectos observables y colaboración entre componentes. Si el código es difícil de testear, esa fricción se interpreta como feedback de diseño.

Desde esta perspectiva, TDD no es solo una técnica de verificación; es una herramienta de diseño incremental.

## TDD vs Test Driven Design

TDD suele describirse como:

```text
Red -> Green -> Refactor
```

Pero “Test Driven Design” enfatiza qué ocurre en cada fase desde el punto de vista del diseño:

- **Red**: diseñar el próximo comportamiento desde afuera.
- **Green**: implementar lo mínimo para validar la idea.
- **Refactor**: mejorar estructura, nombres, responsabilidades y acoplamiento con la seguridad de los tests.

El resultado buscado no es solo una suite de tests, sino un diseño más simple, modular y adaptable.

## Kent Beck: diseño incremental y simplicidad

Kent Beck popularizó TDD en *Test Driven Development: By Example*.

Su formulación central se apoya en dos reglas:

1. No escribir código de producción salvo para hacer pasar una prueba automatizada que falla.
2. Eliminar duplicación.

El diseño no se define completamente al principio. Emerge mediante ciclos pequeños:

1. Se expresa un comportamiento con una prueba.
2. Se implementa lo mínimo.
3. Se refactoriza para mejorar el diseño.

Esto conecta con las reglas de diseño simple asociadas a Beck:

1. Pasa los tests.
2. No contiene duplicación.
3. Expresa la intención del programador.
4. Tiene la menor cantidad necesaria de clases, métodos y piezas.

Desde Test Driven Design, estas reglas son importantes porque impiden dos extremos:

- diseño grande por adelantado sin feedback real;
- código que solo “funciona” pero se degrada internamente.

## Dave Farley: TDD como técnica de diseño

Dave Farley insiste en que TDD es “solo parcialmente sobre testing”. Para él, su valor mayor está en el impacto sobre el diseño.

Farley suele presentar TDD como **Test Driven Design** porque las pruebas:

- fuerzan modularidad;
- reducen acoplamiento;
- aumentan cohesión;
- empujan hacia separación de responsabilidades;
- mejoran encapsulación e information hiding;
- permiten feedback rápido y continuo.

En su enfoque, TDD es una práctica esencial para Continuous Delivery: si el diseño permite tests rápidos, confiables y automatizados, entonces también permite entregar cambios con menor riesgo.

## Outside-in design

Una forma fuerte de Test Driven Design es el enfoque **outside-in**.

En vez de empezar por clases internas, se empieza desde el comportamiento visible:

1. Una prueba de aceptación o prueba externa describe qué debe lograr el sistema.
2. Esa prueba falla porque todavía no existe el comportamiento.
3. Se baja de nivel hacia componentes internos.
4. Se crean unidades y colaboraciones necesarias para hacer pasar el comportamiento.
5. Se refactoriza manteniendo verde la suite.

Este estilo aparece claramente en *Growing Object-Oriented Software, Guided by Tests* de Steve Freeman y Nat Pryce.

La idea no es testear “métodos”, sino diseñar objetos y roles a partir de comportamientos observables.

## Mocks como herramienta de diseño

En Test Driven Design, los mocks no deberían usarse solo para aislar dependencias. En el enfoque mockist/outside-in, los mocks ayudan a descubrir **roles** e **interfaces**.

La idea de “mock roles, not objects” dice que los mocks deberían representar contratos de colaboración, no detalles accidentales de implementación.

Un buen uso de mocks puede ayudar a responder:

- ¿Qué necesita este objeto de sus colaboradores?
- ¿Qué responsabilidad debería vivir en otro componente?
- ¿Cuál es el protocolo mínimo entre dos partes?
- ¿La colaboración tiene un nombre claro?

Pero hay un riesgo: mocks demasiado detallados pueden acoplar las pruebas a la implementación. Eso vuelve más difícil refactorizar.

## Classicist vs mockist TDD

Martin Fowler distingue dos estilos:

### Classicist TDD

- Prefiere probar estado y resultados observables.
- Usa objetos reales siempre que sea práctico.
- Tiende a acoplar menos las pruebas a colaboraciones internas.
- Puede requerir tests más integrados.

### Mockist TDD

- Diseña desde interacciones entre objetos.
- Usa mocks para especificar colaboraciones.
- Favorece outside-in design.
- Puede descubrir interfaces más temprano.
- Puede volver las pruebas frágiles si se mockean detalles internos.

Ambos estilos pueden servir para Test Driven Design. La clave es que las pruebas den feedback útil sin convertirse en una camisa de fuerza.

## Testabilidad como feedback de diseño

Michael Feathers y otros autores destacan que la dificultad para testear suele indicar problemas de diseño.

Señales típicas:

- clases que requieren levantar toda la aplicación para probarse;
- constructores con demasiadas dependencias;
- dependencias globales o estáticas difíciles de reemplazar;
- lógica mezclada con I/O, red, base de datos o UI;
- efectos secundarios ocultos;
- objetos con demasiadas responsabilidades;
- tests muy largos o difíciles de preparar.

En Test Driven Design, estas señales no se tratan como “problemas de testing”, sino como información sobre el diseño.

## Relación con arquitectura

Test Driven Design no elimina la necesidad de arquitectura. Más bien cambia la forma de validarla.

Una arquitectura saludable para TDD suele tener:

- límites claros;
- dependencias dirigidas hacia adentro o hacia abstracciones estables;
- lógica de dominio separada de infraestructura;
- interfaces pequeñas;
- módulos testeables sin levantar todo el sistema;
- tests rápidos en los niveles bajos;
- pocos tests end-to-end amplios y costosos.

Esto conecta con ideas como:

- hexagonal architecture;
- ports and adapters;
- clean architecture;
- functional core, imperative shell;
- test pyramid;
- walking skeleton.

No porque TDD exija una arquitectura específica, sino porque TDD presiona hacia diseños que puedan validarse rápido.

## Relación con Continuous Delivery

Dave Farley conecta TDD con Continuous Delivery.

Para entregar software de forma frecuente y segura se necesita feedback automatizado. Pero para que ese feedback sea rápido y confiable, el sistema debe estar diseñado para ser testeable.

Por eso TDD ayuda a CD en dos niveles:

1. **Nivel de código**: unidades pequeñas, bajo acoplamiento, responsabilidades claras.
2. **Nivel de entrega**: suites automatizadas que dan confianza para integrar y desplegar.

Una pipeline saludable no debería depender exclusivamente de tests end-to-end lentos y frágiles. TDD ayuda a construir una base amplia de tests rápidos.

## Prácticas recomendadas

### 1. Empezar por comportamiento, no por implementación

Una buena prueba expresa qué debe pasar, no cómo está implementado.

Preferir:

```text
cuando el input está vacío, el parser devuelve una lista vacía
```

sobre:

```text
el método parse llama a readToken tres veces
```

salvo que la colaboración sea realmente parte del diseño público entre objetos.

### 2. Diseñar APIs desde el test

El test es el primer consumidor del código. Si el test se lee mal, probablemente la API también.

Preguntas útiles:

- ¿El nombre expresa intención?
- ¿Los argumentos son naturales?
- ¿El resultado es claro?
- ¿Hay demasiada configuración accidental?
- ¿La prueba necesita conocer demasiado?

### 3. Mantener ciclos pequeños

Un ciclo efectivo debería durar minutos:

```text
red pequeño -> green simple -> refactor seguro
```

Si el ciclo se vuelve grande, aparece diseño especulativo o debugging largo.

### 4. Refactorizar cuando está verde

La fase de refactor no es opcional. Sin refactor, TDD puede producir código cubierto por tests pero mal diseñado.

Refactors típicos:

- renombrar;
- extraer función;
- extraer clase;
- eliminar duplicación;
- separar lógica pura de efectos;
- reducir dependencias;
- simplificar interfaces.

### 5. Evitar mocks de detalles internos

Mockear detalles accidentales puede bloquear refactors.

Usar mocks preferentemente en límites relevantes:

- servicios externos;
- clock/time;
- filesystem;
- red;
- base de datos;
- publicadores de eventos;
- contratos entre componentes significativos.

### 6. Combinar niveles de tests

Test Driven Design no significa “solo unit tests”. Una estrategia sólida puede incluir:

- pruebas unitarias rápidas;
- pruebas de componentes;
- pruebas de contrato;
- pruebas de aceptación;
- algunas pruebas end-to-end.

El punto es que cada nivel tenga un propósito claro.

## Antipatrones

### TDD como cobertura después del código

Si los tests se escriben al final, pueden verificar, pero ya no guiaron el diseño.

### Tests demasiado acoplados a implementación

Si cada refactor rompe muchos tests aunque el comportamiento no cambió, las pruebas están demasiado cerca de los detalles internos.

### Mockear todo

Mockear cada clase puede producir tests frágiles, diseño artificial y demasiadas interfaces innecesarias.

### No refactorizar

Red/Green sin Refactor genera código que pasa tests pero acumula deuda.

### Tests lentos como base del flujo

Si cada ciclo tarda mucho, TDD deja de funcionar como feedback de diseño.

### Diseño emergente sin criterio

TDD no reemplaza pensamiento de diseño. Requiere criterio sobre nombres, límites, responsabilidades y arquitectura.

## Heurísticas de buen Test Driven Design

Un buen ciclo de TDD está funcionando como diseño si:

- los tests se leen como ejemplos claros;
- el código productivo tiene menos duplicación después de refactorizar;
- las unidades tienen responsabilidades pequeñas;
- las dependencias son explícitas;
- la mayoría de tests son rápidos;
- se puede cambiar implementación sin romper muchos tests;
- los errores de diseño aparecen temprano como tests difíciles de escribir;
- el sistema puede evolucionar sin grandes reescrituras.

## Relación con commits atómicos

Test Driven Design combina bien con commits atómicos.

Una opción es commitear el ciclo completo como un cambio lógico:

```text
fix(parser): handle empty input
```

incluyendo prueba, implementación y refactor.

Otra opción, útil cuando se quiere mostrar evolución, es separar commits atómicos por intención:

```text
test(parser): describe empty input behavior
fix(parser): handle empty input
refactor(parser): remove duplicate token handling
```

La regla práctica: cada commit debe dejar el repo en estado consistente y contar una intención clara.

## Relación con Conventional Commits

Si usamos Conventional Commits, el tipo ayuda a validar si el cambio es atómico:

- `test`: agrega o ajusta tests sin cambio productivo directo.
- `fix`: corrige comportamiento.
- `feat`: agrega comportamiento nuevo.
- `refactor`: mejora diseño sin cambiar comportamiento.
- `docs`: documenta decisiones o uso.

Si un commit necesita varios tipos para describirse, probablemente conviene dividirlo.

## Recomendaciones para este repo

1. Tratar TDD como herramienta de diseño, no solo como cobertura.
2. Escribir primero el ejemplo/test del comportamiento esperado.
3. Mantener ciclos cortos: Red, Green, Refactor.
4. Refactorizar activamente cuando la suite está verde.
5. Preferir tests rápidos y cercanos al dominio.
6. Separar lógica de dominio de I/O e infraestructura.
7. Usar mocks con intención de diseño, no por costumbre.
8. Mantener commits atómicos que reflejen el ciclo o una intención clara.
9. Documentar decisiones de diseño cuando un test revele una interfaz importante.

## Fuentes

- [Kent Beck, *Test Driven Development: By Example*, InformIT](https://www.informit.com/store/test-driven-development-by-example-9780321146533)
- [Martin Fowler, “Beck Design Rules”](https://www.martinfowler.com/bliki/BeckDesignRules.html)
- [Dave Farley, “Test Driven Development”](https://www.davefarley.net/?p=220)
- [Dave Farley, “The basics of TDD”](https://www.davefarley.net/?p=180)
- [Scrum Guide Expansion Pack, “Software Engineering Practices”](https://scrumexpansion.org/software-engineering-practices/)
- [CD.Training, “TDD & BDD - Design Through Testing”](https://courses.cd.training/courses/tdd-bdd-design-through-testing)
- [Steve Freeman y Nat Pryce, *Growing Object-Oriented Software, Guided by Tests*](https://growing-object-oriented-software.com/)
- [Pearson, *Growing Object-Oriented Software, Guided by Tests*](https://www.pearson.com/en-us/subject-catalog/p/growing-object-oriented-software-guided-by-tests/P200000009298)
- [jMock/OOPSLA, “Mock Roles, not Objects”](https://jmock.org/oopsla2004.pdf)
- [Martin Fowler, “Mocks Aren’t Stubs”](https://martinfowler.com/articles/mocksArentStubs.html)
- [James Shore, “How Does TDD Affect Design?”](https://www.jamesshore.com/v2/blog/2014/how-does-tdd-affect-design)
- [James Shore, “Testing Without Mocks”](https://www.jamesshore.com/v2/projects/nullables/testing-without-mocks)
- [Michael Feathers, “The Bar is Higher Now”](https://www.artima.com/weblogs/viewpost.jsp?thread=42486)
- [Martin Fowler, “Test Pyramid”](https://martinfowler.com/bliki/TestPyramid.html)
- [Dave Farley, “Acceptance Test Driven Development Guide”](https://continuous-delivery.co.uk/downloads/ATDD%20Guide%2026-03-21.pdf)
