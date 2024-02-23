from flask import Flask
import yfinance as yf

app = Flask(__name__)

@app.route('/')
def health_check():
    return '<h1>Healthy</h1>'
@app.route("/splits/<ticker>")
def splits(ticker):
    aapl = yf.Ticker(ticker)
    return aapl.splits.to_csv()

@app.route("/dividends/<ticker>")
def dividends(ticker):
    aapl = yf.Ticker(ticker)
    return aapl.dividends.to_csv()

@app.route("/history/<ticker>")
def history(ticker):
    data = yf.download(ticker)
    app.logger.info(data.to_csv())
    return data.to_csv()

@app.route("/ticker/<ticker>")
def ticker(ticker):
    data = yf.download(ticker)
    stock = yf.Ticker(ticker)
    history = []
    for i, r in data.iterrows():
        price = {
            "trading_day": f"{i.date()}",
            "open": r["Open"],
            "high": r["High"],
            "low": r["Low"],
            "close": r["Close"],
            "adjusted_close": r["Adj Close"],
            "volume": r["Volume"],
        }
        history.append(price)
    
    dividends = []
    splits = []
    for t, split in stock.splits.items():
        splits.append({ "trading_day": f"{t.date()}", "split": split})
    for t, dividend in stock.dividends.items():
        dividends.append({ "effective_date": f"{t.date()}", "dividend": dividend})
    return {
        "history": history,
        "splits": splits,
        "dividends": dividends,
    }
    