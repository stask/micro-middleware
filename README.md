micro-middleware
================

Collection of small middlewares.

## wrap-json-params

Parses request body as JSON if content type is 'application/json'.
If `:hyphenize` is `true`, will replace underscores with hyphens in keys. Default is false.
Will decompress body before parsing if 'Content-Encoding' is 'gzip'.

```clojure
(-> routes
    ...; other middlewares
    (wrap-json-params :hyphenize true))
```

See tests for more examples

## wrap-json-response

Converts body (array or hashmap) to JSON string if client accepts json.
If `:dehyphenize` is `true`, will replace hyphens with underscores in keys. Default is false.
Has custom formatter for joda time.

```clojure
(-> routes
    ...; other middlewares
    (wrap-json-response :dehyphenize true))
```

See tests for more examples

##
