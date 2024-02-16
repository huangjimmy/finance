# Finance - Backtest / Train with Historical Stock Price

## Introduction

## Concepts

## Supported Exchanges

* NYSE
* NASDAQ
* TSX
* AMEX

## Prerequisites

* Install [homebrew](https://brew.sh/)
* Install [asdf](https://asdf-vm.com/guide/getting-started.html#_2-download-asdf)
* Install [asdf nodejs plugin](https://github.com/asdf-vm/asdf-nodejs)
* Install nodejs by running below command in the directory where .tool-versions resides
```bash
asdf install nodejs
* ```

## Database

The **finance** app uses hsqldb file database

To run database migration

```bash
 ./gradlew update
```

To generate jOOQ POJOs, DAOs
```bash
 ./gradlew jooqCodegen
```

## Build

To run finance app
```bash
 ./gradlew bootRun
```

## License