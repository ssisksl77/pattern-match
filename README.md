# yhnam/pattern-match

similar to `core.match` but less optimized, more extendable.

## Usage

### :where

`yhnam/pattern-match` doesn't have guards but `:where`
``` clj
(require '[yhnam.pattern-match :as pm])
(match [1 2]
      [a b] :where (do (println a b "왓숑...") false)
      "HI"

      [c d] :where (do (println c d "여디도 왔엉") false)
      "GOOD"

      [e f] :where (do (println e f "여기 마지막") true)
      "MOOYAHO"

      :else
      "NEVER")
;; => "MOOYAHO"
```

### just throw Exception

``` clj
(require '[yhnam.pattern-match :as pm')
(match [1 2]
      [a b] (if (= a 1) (throw backtrack-exception))
      [c e] "HERE"
      :else "ELSE")
;; => "HERE"
```

Reason why `core.match` can't support this is, even though implementation is absolutely same, optimization.
`core.match` will remove unnecessary patterns (`[c e]`, `:else`).

but they support `guard` which is not enough.



### Test

Will be Soon

    $ clojure -T:build test

### CI

Run the project's CI pipeline and build a JAR (this will fail until you edit the tests to pass):

    $ clojure -T:build ci

This will produce an updated `pom.xml` file with synchronized dependencies inside the `META-INF`
directory inside `target/classes` and the JAR in `target`. You can update the version (and SCM tag)
information in generated `pom.xml` by updating `build.clj`.

Install it locally (requires the `ci` task be run first):

    $ clojure -T:build install

Deploy it to Clojars -- needs `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` environment
variables (requires the `ci` task be run first):

    $ clojure -T:build deploy

Your library will be deployed to net.clojars.yhnam/pattern-match on clojars.org by default.

## License

Distributed under the Eclipse Public License version 1.0.
