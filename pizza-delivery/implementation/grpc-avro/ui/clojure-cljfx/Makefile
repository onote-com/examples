SHELL := bash
.ONESHELL:
.SHELLFLAGS := -eu -o pipefail -c
.DELETE_ON_ERROR:
MAKEFLAGS += --warn-undefined-variables
MAKEFLAGS += --no-builtin-rules

# .env file support
ifneq (,$(wildcard ./.env))
	include .env
	export
endif

# Dev Util

CLOJURE_BUILD:=clojure -T:build

.PHONY: bootstrap
bootstrap:
	brew install clojure tokei

.PHONY: outdated
outdated:
	clojure -M:outdated

.PHONY: deps
deps: deps.edn
	clojure -Stree

# Dev Workflow

target/classes/com/example/pizza/PizzaDelivery.class: resources/avro/Pizza\ Delivery.avpr
	$(CLOJURE_BUILD) compile-java-from-avro-protocol

CLJ_REPL_ALIAS:=

.PHONY: dev
dev: target/classes/com/example/pizza/PizzaDelivery.class
	clojure -M:dev${CLJ_REPL_ALIAS}

.PHONY: test
test: target/classes/com/example/pizza/PizzaDelivery.class
	$(CLOJURE_BUILD) test

.PHONY: run
run: target/classes/com/example/pizza/PizzaDelivery.class
	clojure -M:run

# Release

VERSION:=$(shell git rev-parse --short=10 HEAD)

.PHONY: clean
clean:
	rm -rf src/main/generated-java
	$(CLOJURE_BUILD) clean

target/uberjar/clojure-cljfx-ui-*.jar: target/classes/com/example/pizza/PizzaDelivery.class
	$(CLOJURE_BUILD) ci

target/package/clojure-cljfx-ui/: target/uberjar/clojure-cljfx-ui-*.jar
	jpackage -t app-image -i target/uberjar/ -d target/package/ -n clojure-cljfx-ui --main-class com.onote.example.pizza_delivery_ui --main-jar clojure-cljfx-ui-0.1.0-SNAPSHOT.jar

package: target/package/clojure-cljfx-ui/

# Project info

.PHONY: loc
loc:
	tokei resources/ src/ Makefile *.edn *.clj
