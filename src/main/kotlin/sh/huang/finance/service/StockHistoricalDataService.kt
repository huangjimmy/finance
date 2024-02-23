package sh.huang.finance.service

import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sh.huang.finance.constant.ExchangeConstant
import sh.huang.finance.dataproviders.eodhd.EodhdStockDailyData
import sh.huang.finance.dataproviders.yfinance.YahooFinanceClient
import sh.huang.finance.generated.tables.StockDividends.STOCK_DIVIDENDS
import sh.huang.finance.generated.tables.StockSplits.STOCK_SPLITS
import sh.huang.finance.generated.tables.StockHistoricalPrice.STOCK_HISTORICAL_PRICE
import sh.huang.finance.generated.tables.daos.StockDividendsDao
import sh.huang.finance.generated.tables.daos.StockHistoricalPriceDao
import sh.huang.finance.generated.tables.daos.StockSplitsDao
import sh.huang.finance.generated.tables.pojos.StockDividends
import sh.huang.finance.generated.tables.pojos.StockHistoricalPrice
import sh.huang.finance.generated.tables.pojos.StockSplits
import sh.huang.finance.generated.tables.pojos.StockSymbol
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


@Service
class StockHistoricalDataService {
    @Autowired
    private lateinit var eodhdApiService: EodhdApiService
    @Autowired
    private lateinit var stockSymbolService: StockSymbolService
    @Autowired
    private lateinit var stockHistoricalPriceDao: StockHistoricalPriceDao
    @Autowired
    private lateinit var stockDividendsDao: StockDividendsDao
    @Autowired
    private lateinit var stockSplitsDao: StockSplitsDao
    @Autowired
    private lateinit var yahooFinanceClient: YahooFinanceClient

    @Transactional
    fun syncHistoricalYFinance(symbol: StockSymbol) {
        runBlocking {
            val yahooFinanceStockData = yahooFinanceClient.getStock(symbol.symbol, symbol.exchange)
            val splitsMap = mutableMapOf<Date, Double>()
            val dividendsMap = mutableMapOf<Date, Double>()

            for (split in yahooFinanceStockData.splits) {
                val tradingDay = split.tradingDay
                val ratio = split.split
                splitsMap[tradingDay] = ratio

                val splitData = StockSplits()
                splitData.symbol = symbol.symbol
                splitData.exchange = symbol.exchange
                splitData.tradingDay = tradingDay.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                splitData.stockSplitEffectiveDate = splitData.tradingDay
                splitData.stockSplitBefore = BigDecimal(1)
                splitData.stockSplitAfter = BigDecimal(ratio)

                val existingSplits = stockSplitsDao.ctx().selectFrom(STOCK_SPLITS).where(STOCK_SPLITS.SYMBOL.eq(symbol.symbol)
                    .and(STOCK_SPLITS.EXCHANGE.eq(symbol.exchange).and(
                        STOCK_SPLITS.TRADING_DAY.eq(splitData.tradingDay)
                    )))
                    .fetchInto(StockSplits::class.java)
                if (existingSplits.isEmpty()) stockSplitsDao.insert(splitData)
                else {
                    val existingSplit = existingSplits.first()
                    existingSplit.stockSplitBefore = splitData.stockSplitAfter
                    existingSplit.stockSplitAfter = splitData.stockSplitAfter
                    stockSplitsDao.update(existingSplit)
                }
            }

            for (dividend in yahooFinanceStockData.dividends) {
                val effectiveDate = dividend.effectiveDate
                val amount = dividend.dividend
                dividendsMap[effectiveDate] = amount
            }

            var reverseAdjustRatio = 1.0
            // calculate raw unadjusted price and dividends

            for (history in yahooFinanceStockData.history.reversed()) {
                val tradingDay = history.tradingDay
                val open = history.open * reverseAdjustRatio
                val high = history.high * reverseAdjustRatio
                val low = history.low * reverseAdjustRatio
                val close = history.close * reverseAdjustRatio // close adjusted by splits
                val volume = history.volume / reverseAdjustRatio
                val adjustedCloseSplits = history.close
                val adjustedCloseSplitsAndDividend = history.adjustedClose // adjusted by splits and dividends

                val historicalPrice = StockHistoricalPrice()
                historicalPrice.tradingDay = tradingDay.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                historicalPrice.symbol = symbol.symbol
                historicalPrice.exchange = symbol.exchange
                historicalPrice.open = BigDecimal(open).setScale(4, RoundingMode.HALF_UP)
                historicalPrice.high = BigDecimal(high).setScale(4, RoundingMode.HALF_UP)
                historicalPrice.low = BigDecimal(low).setScale(4, RoundingMode.HALF_UP)
                historicalPrice.close = BigDecimal(close).setScale(4, RoundingMode.HALF_UP)
                historicalPrice.volume = BigDecimal(volume).setScale(4, RoundingMode.HALF_UP)

                var amount = dividendsMap[tradingDay]
                if (amount != null){
                    amount *= reverseAdjustRatio
                    dividendsMap[tradingDay] = amount
                }

                val ratio = splitsMap[tradingDay]
                if (ratio != null) {
                    reverseAdjustRatio *= ratio
                }

                val existingHistories = stockHistoricalPriceDao.ctx().selectFrom(STOCK_HISTORICAL_PRICE)
                    .where(STOCK_HISTORICAL_PRICE.SYMBOL.eq(symbol.symbol)
                        .and(STOCK_HISTORICAL_PRICE.EXCHANGE.eq(symbol.exchange)
                            .and(STOCK_HISTORICAL_PRICE.TRADING_DAY.eq(historicalPrice.tradingDay))
                    )).fetchInto(StockHistoricalPrice::class.java)
                if (existingHistories.size == 0) stockHistoricalPriceDao.insert(historicalPrice)
                else {
                    val existingHistory = existingHistories.first()
                    existingHistory.open = historicalPrice.open
                    existingHistory.high = historicalPrice.high
                    existingHistory.low = historicalPrice.low
                    existingHistory.close = historicalPrice.close
                    existingHistory.volume = historicalPrice.volume
                    stockHistoricalPriceDao.update(existingHistory)
                }
            }

            // upsert dividend info
            for(dividend in yahooFinanceStockData.dividends) {
                val date = dividend.effectiveDate
                val amount = dividendsMap[date] ?: continue
                val dividendRaw = StockDividends()
                dividendRaw.symbol = symbol.symbol
                dividendRaw.exchange = symbol.exchange
                dividendRaw.tradingDay = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                dividendRaw.dividendPerShare = BigDecimal(amount).setScale(4, RoundingMode.HALF_UP)

                val existingDividends = stockDividendsDao.ctx().selectFrom(STOCK_DIVIDENDS).where(STOCK_DIVIDENDS.SYMBOL.eq(symbol.symbol)
                    .and(STOCK_DIVIDENDS.EXCHANGE.eq(symbol.exchange).and(
                        STOCK_DIVIDENDS.TRADING_DAY.eq(dividendRaw.tradingDay)
                    )))
                    .fetchInto(StockDividends::class.java)
                if (existingDividends.size == 0) stockDividendsDao.insert(dividendRaw)
                else {
                    val existingDividend = existingDividends.first()
                    existingDividend.dividendPerShare = dividendRaw.dividendPerShare
                    stockDividendsDao.update(existingDividend)
                }
            }
        }
    }

    @Transactional
    fun syncHistoricalPriceMCDUS() {
        runBlocking {
            val MCD = stockSymbolService.findStockSymbol( "MCD", null, null, null)
                    .filter { it.exchange == ExchangeConstant.NYSE || it.exchange == ExchangeConstant.NASDAQ }
                    .first()
            val dailyData = eodhdApiService.historicalPrice(MCD)

            fun eodhdToHistoricalPrice(eodhdStockDailyData: EodhdStockDailyData): StockHistoricalPrice {
                val price = StockHistoricalPrice()
                with(price) {
                    symbol = MCD.symbol
                    exchange = MCD.exchange
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    tradingDay = LocalDate.parse(eodhdStockDailyData.date, formatter)
                    open = BigDecimal(eodhdStockDailyData.open).setScale(2, RoundingMode.HALF_UP)
                    high = BigDecimal(eodhdStockDailyData.high).setScale(2, RoundingMode.HALF_UP)
                    low = BigDecimal(eodhdStockDailyData.low).setScale(2, RoundingMode.HALF_UP)
                    close = BigDecimal(eodhdStockDailyData.close).setScale(2, RoundingMode.HALF_UP)
                    volume = BigDecimal(eodhdStockDailyData.volume)
                }
                return price
            }

            val historicalPrice = dailyData.map {
                eodhdToHistoricalPrice(it)
            }
            historicalPrice.forEach {
                stockHistoricalPriceDao.insert(it)
            }
        }
    }

    fun historicalPriceMCDUS(): List<StockHistoricalPrice> {
        return stockHistoricalPriceDao.findAll()
    }

    fun historicalPrice(symbol: StockSymbol): List<StockHistoricalPrice> {
        return stockHistoricalPriceDao.ctx().selectFrom(STOCK_HISTORICAL_PRICE).where(
            STOCK_HISTORICAL_PRICE.SYMBOL.eq(symbol.symbol).and(
                STOCK_HISTORICAL_PRICE.EXCHANGE.eq(symbol.exchange)
            )
        ).fetchInto(StockHistoricalPrice::class.java)
    }

    fun splitHistory(symbol: StockSymbol): List<StockSplits> {
        return stockSplitsDao.ctx().selectFrom(STOCK_SPLITS).where(
            STOCK_SPLITS.SYMBOL.eq(symbol.symbol).and(
                STOCK_SPLITS.EXCHANGE.eq(symbol.exchange)
            )
        ).fetchInto(StockSplits::class.java)
    }

    fun dividendHistory(symbol: StockSymbol): List<StockDividends> {
        return stockDividendsDao.ctx().selectFrom(STOCK_DIVIDENDS).where(
            STOCK_DIVIDENDS.SYMBOL.eq(symbol.symbol).and(
                STOCK_DIVIDENDS.EXCHANGE.eq(symbol.exchange)
            )
        ).fetchInto(StockDividends::class.java)
    }
}