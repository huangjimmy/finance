package sh.huang.finance.job

import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import sh.huang.finance.service.StockHistoricalDataService
import sh.huang.finance.service.StockSymbolService
import java.text.SimpleDateFormat
import java.time.Duration

@Component
class StockHistorySyncJob {
    private val log: Logger = LoggerFactory.getLogger(StockHistorySyncJob::class.java)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    @Autowired
    private lateinit var stockHistoricalDataService: StockHistoricalDataService
    @Autowired
    private lateinit var stockSymbolService: StockSymbolService

    companion object Constants {
        const val TIME_ZONE = "America/Los_Angeles"
    }

    @Scheduled(cron = "0 15 20 ? * MON-FRI", zone = TIME_ZONE)
    fun syncStockHistory() = runBlocking{
        val symbols = stockSymbolService.findStockSymbol(null, null, null, null)
        symbols.forEach { symbol ->
            log.info("Sync history of ${symbol.symbol} ${symbol.exchange} begin")
            try {
                Thread.sleep(Duration.ofSeconds(1))
                stockHistoricalDataService.syncHistoricalYFinance(symbol)
                log.info("Sync history of ${symbol.symbol} ${symbol.exchange} completed")
            } catch (e: Exception) {
               log.error("Failed to sync history of ${symbol.symbol} ${symbol.exchange}", e)
            }
        }
    }
}
