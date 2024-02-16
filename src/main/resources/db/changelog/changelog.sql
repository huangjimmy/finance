-- liquibase formatted sql

-- changeset liquibase:1
CREATE TABLE test_table (test_id INT, test_column VARCHAR(16), PRIMARY KEY (test_id))
-- changeset liquibase:2
CREATE TABLE StockSymbol (id BIGINT IDENTITY, symbol VARCHAR(30), name VARCHAR(255), exchange VARCHAR(10), PRIMARY KEY(id), UNIQUE(symbol, exchange))
-- changeset liquibase:3
ALTER TABLE StockSymbol RENAME TO Stock_Symbol
-- changeset liquibase:4
CREATE TABLE Stock_Historical_Price(id BIGINT IDENTITY PRIMARY KEY,
trading_day DATE,
exchange VARCHAR(10), symbol VARCHAR(30),
"open" numeric(16,4), "close" numeric(16,4), high numeric(16,4), low numeric(16,4), volume numeric(16,4),
 UNIQUE(trading_day, exchange, symbol));

CREATE TABLE Stock_Dividends(id BIGINT IDENTITY PRIMARY KEY,
 exchange VARCHAR(10), symbol VARCHAR(30),
 trading_day DATE,
 declaration_date DATE, ex_dividend_date DATE, record_date DATE, payment_date DATE, dividend_per_share numeric(10, 4),
UNIQUE(trading_day, exchange, symbol));

 CREATE TABLE Stock_Dividend_Splits(id BIGINT IDENTITY PRIMARY KEY,
  exchange VARCHAR(10), symbol VARCHAR(30),
  trading_day DATE,
  stock_split_effective_date DATE, stock_split_before numeric(10, 2), stock_split_after numeric(10, 2),
 UNIQUE(trading_day, exchange, symbol));
-- changeset liquibase:5
ALTER TABLE Stock_Dividend_Splits RENAME TO Stock_Splits;
-- changeset liquibase:6
ALTER TABLE Stock_Symbol ADD COLUMN CURRENCY VARCHAR(10);