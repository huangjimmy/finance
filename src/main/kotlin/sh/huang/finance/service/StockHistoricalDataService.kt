package sh.huang.finance.service

import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sh.huang.finance.constant.ExchangeConstant
import sh.huang.finance.dataproviders.eodhd.EodhdStockDailyData
import sh.huang.finance.generated.tables.daos.StockHistoricalPriceDao
import sh.huang.finance.generated.tables.pojos.StockHistoricalPrice
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Service
class StockHistoricalDataService {
    @Autowired
    private lateinit var eodhdApiService: EodhdApiService
    @Autowired
    private lateinit var stockSymbolService: StockSymbolService
    @Autowired
    private lateinit var stockHistoricalPriceDao: StockHistoricalPriceDao

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
}