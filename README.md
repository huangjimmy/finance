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
* Install nodejs and python by running below command in the directory where .tool-versions resides
```bash
asdf install
```

* Create a python venv and activate venv

```bash
python -m venv .venv  
source .venv/bin/activate
```

* Install necessary python deps such as yfinance

```bash
pip install -r requirements.txt
poetry install --no-root
```

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

First ensure there is an instance of PostgreSQL running at localhost:5432 
* with username postgres and empty password
* with database `finance` created


then start the flask yahoo finance app

```bash
 cd yfinance
 flask run --port 5001
```

then in another terminal, start the finance app

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

## License