package sh.huang.finance.job

import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import sh.huang.finance.constant.ExchangeConstant
import sh.huang.finance.service.NasdaqNyseAmexSymbolService
import sh.huang.finance.service.TsxSymbolService
import java.text.SimpleDateFormat



@Component
class StockSymbolSyncJob (val nasdaqNyseAmexSymbolService: NasdaqNyseAmexSymbolService, val tsxSymbolService: TsxSymbolService) {
    private val log: Logger = LoggerFactory.getLogger(StockSymbolSyncJob::class.java)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    companion object Constants {
        const val TIME_ZONE = "America/Los_Angeles"
    }

    @Scheduled(cron = "0 0 20 ? * MON-FRI", zone = TIME_ZONE)
    fun syncNyseSymbol() = runBlocking{
        val symbols = async { nasdaqNyseAmexSymbolService.retrieveSymbols(ExchangeConstant.NYSE) }.await()
        nasdaqNyseAmexSymbolService.saveSymbols(symbols, ExchangeConstant.NYSE)
        log.info(symbols.first().toString())
    }

    @Scheduled(cron = "0 0 19 ? * MON-FRI", zone = TIME_ZONE)
    fun syncNasdaqSymbol() = runBlocking{
        val symbols = async { nasdaqNyseAmexSymbolService.retrieveSymbols(ExchangeConstant.NASDAQ) }.await()
        nasdaqNyseAmexSymbolService.saveSymbols(symbols, ExchangeConstant.NASDAQ)
        log.info(symbols.first().toString());
    }

    @Scheduled(cron = "0 0 21 ? * MON-FRI", zone = TIME_ZONE)
    fun syncAmexSymbol() = runBlocking{
        val symbols = async { nasdaqNyseAmexSymbolService.retrieveSymbols(ExchangeConstant.AMEX) }.await()
        nasdaqNyseAmexSymbolService.saveSymbols(symbols, ExchangeConstant.AMEX)
        log.info(symbols.first().toString());
    }

    @Scheduled(cron = "0 0 22 ? * MON-FRI", zone = TIME_ZONE)
    fun syncTsxSymbol() = runBlocking{
        val symbols = async { tsxSymbolService.retrieveSymbols() }.await()
        tsxSymbolService.saveSymbols(symbols)
        log.info(symbols.first().toString());
    }
}