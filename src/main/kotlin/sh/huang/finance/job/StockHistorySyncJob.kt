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

@Component
class StockHistorySyncJob {
    private val log: Logger = LoggerFactory.getLogger(StockSymbolSyncJob::class.java)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    @Autowired
    private lateinit var stockHistoricalDataService: StockHistoricalDataService
    @Autowired
    private lateinit var stockSymbolService: StockSymbolService

    companion object Constants {
        const val TIME_ZONE = "America/Los_Angeles"
    }

    @Scheduled(cron = "0 0 20 ? * MON-FRI", zone = TIME_ZONE)
    fun syncStockHistory() = runBlocking{
        val symbols = stockSymbolService.findStockSymbol(null, null, null, null)
        symbols.forEach { symbol ->
            log.info("Sync history of ${symbol.symbol} ${symbol.exchange}")
            try {
                stockHistoricalDataService.syncHistoricalYFinance(symbol)
            } catch (e: Exception) {
               log.error("Failed to sync history of ${symbol.symbol} ${symbol.exchange}", e)
            }
        }

    }
}