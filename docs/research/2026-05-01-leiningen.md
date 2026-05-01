# Leiningen

Fecha: 2026-05-01

## Nota sobre Context7

En esta sesión no tengo una herramienta Context7 disponible. Para esta
investigación usé documentación oficial en internet y fuentes primarias.

## Resumen

Leiningen, normalmente usado como `lein`, es una herramienta de automatización
para proyectos Clojure. Sirve para crear proyectos, manejar dependencias,
ejecutar tests, abrir REPLs, empaquetar aplicaciones y correr tareas.

En un proyecto Leiningen, el archivo central es:

```text
project.clj
```

Ahí se define el nombre del proyecto, versión, dependencias, paths, perfiles,
plugins y configuración de tareas.

## Estructura típica

Una estructura mínima suele ser:

```text
project.clj
src/<project>/core.clj
test/<project>/core_test.clj
resources/
```

Para este repo, una estructura inicial podría ser:

```text
project.clj
src/pi_clojure/core.clj
test/pi_clojure/core_test.clj
```

En Clojure, los namespaces usan guiones normalmente:

```clojure
(ns pi-clojure.core)
```

pero los paths de archivo usan guion bajo:

```text
src/pi_clojure/core.clj
```

## `project.clj` mínimo

Un `project.clj` mínimo puede verse así:

```clojure
(defproject pi-clojure "0.1.0-SNAPSHOT"
  :description "pi clojure"
  :url "https://github.com/andrestobelem/pi-clojure"
  :license {:name "MIT"}
  :dependencies [[org.clojure/clojure "1.12.4"]]
  :main ^:skip-aot pi-clojure.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
```

La dependencia de Clojure se declara como:

```clojure
[org.clojure/clojure "1.12.4"]
```

## Comandos principales

### Crear un proyecto

```sh
lein new app pi-clojure
```

Para este repo ya existente, probablemente no conviene correr `lein new` encima
sin revisar primero. Es mejor crear los archivos mínimos manualmente.

### Ejecutar tests

```sh
lein test
```

También se puede ejecutar un namespace específico:

```sh
lein test pi-clojure.core-test
```

### Abrir REPL

```sh
lein repl
```

Dentro de un proyecto, el REPL carga el classpath del proyecto: `src`, `test`,
`resources` y dependencias.

### Ejecutar aplicación

Si el proyecto define `:main`, se puede correr:

```sh
lein run
```

### Descargar dependencias

Leiningen descarga dependencias cuando hacen falta, pero también se puede pedir
explícitamente:

```sh
lein deps
```

### Validar compilación básica

```sh
lein check
```

### Limpiar artefactos

```sh
lein clean
```

### Crear uberjar

```sh
lein uberjar
```

Genera un jar autocontenido, usualmente bajo `target/`.

## Tests con `clojure.test`

Leiningen usa `clojure.test` por defecto.

Ejemplo:

```clojure
(ns pi-clojure.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [pi-clojure.core :as core]))

(deftest greeting-test
  (testing "returns a greeting"
    (is (= "Hello, pi-clojure!" (core/greeting)))))
```

Código productivo correspondiente:

```clojure
(ns pi-clojure.core)

(defn greeting []
  "Hello, pi-clojure!")
```

## Dependencias

Las dependencias se agregan en `:dependencies`:

```clojure
:dependencies [[org.clojure/clojure "1.12.4"]
               [cheshire "5.13.0"]]
```

Leiningen resuelve dependencias desde repositorios como Clojars y Maven Central.

## Perfiles

Los perfiles permiten cambiar configuración por contexto.

Ejemplo:

```clojure
:profiles {:dev {:dependencies [[org.clojure/tools.namespace "1.5.0"]]}
           :uberjar {:aot :all}}
```

Leiningen también tiene perfiles task-specific como `:test` y `:repl`.

## Flujo recomendado para este repo

Como este repo usa TDD, trunk-based development, commits atómicos y Conventional
Commits, conviene usar Leiningen así:

1. Crear primero `project.clj` y un test mínimo.
2. Correr `lein test` y ver fallar si estamos agregando comportamiento.
3. Implementar lo mínimo en `src/`.
4. Correr `lein test` hasta verde.
5. Refactorizar con la suite en verde.
6. Hacer commit atómico.

Comandos frecuentes:

```sh
lein test
lein repl
lein check
```

## Recomendación inicial

Para inicializar este repo con Leiningen sin pisar archivos existentes:

1. Crear `project.clj`.
2. Crear `src/pi_clojure/core.clj`.
3. Crear `test/pi_clojure/core_test.clj`.
4. Verificar con:

   ```sh
   lein test
   ```

5. Documentar los comandos en `README.md`.

## Fuentes

- [Leiningen Tutorial](https://leiningen.org/tutorial.html)
- [Leiningen Home / Documentation](https://leiningen.org/)
- [Leiningen Profiles](https://leiningen.org/profiles.html)
- [Leiningen sample.project.clj](https://github.com/technomancy/leiningen/blob/stable/sample.project.clj)
- [Clojure Downloads](https://www.clojure.org/releases/downloads)
- [Clojure REPL guide](https://clojure.org/guides/repl/launching_a_basic_repl)
- [clojure.test API](https://clojure.github.io/clojure/clojure.test-api.html)
