.PHONY: test

repl:
	clojure -M:repl

test:
	clojure -M:test

run:
	clojure -M:run


ui-deps:
	npm install

ui-dev:
	clj -M:shadow:ui:ui-repl watch app

ui-build:
	clj -M:shadow:ui release app
