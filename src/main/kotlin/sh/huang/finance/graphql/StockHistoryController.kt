package sh.huang.finance.graphql

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import sh.huang.finance.generated.tables.pojos.StockDividends
import sh.huang.finance.generated.tables.pojos.StockHistoricalPrice
import sh.huang.finance.generated.tables.pojos.StockSplits
import sh.huang.finance.service.StockHistoricalDataService
import sh.huang.finance.service.StockSymbolService

@Controller
class StockHistoryController {
    @Autowired
    private lateinit var stockHistoricalDataService: StockHistoricalDataService
    @Autowired
    private lateinit var stockSymbolService: StockSymbolService

    @MutationMapping
    fun syncHistoryOf(@Argument symbol: String, @Argument exchange: String): String {
        val stockSymbols = stockSymbolService.findStockSymbol(symbol, null, exchange, null)
        if (stockSymbols.isEmpty()) return "SYMBOL NOT FOUND"
        val stockSymbol = stockSymbols.first()
        stockHistoricalDataService.syncHistoricalYFinance(stockSymbol)
        return "OK"
    }

    @QueryMapping
    fun historicalPriceOf(@Argument symbol: String, @Argument exchange: String): List<StockHistoricalPrice> {
        val stockSymbols = stockSymbolService.findStockSymbol(symbol, null, exchange, null)
        if (stockSymbols.isEmpty()) return listOf()
        val stockSymbol = stockSymbols.first()
        return stockHistoricalDataService.historicalPrice(stockSymbol)
    }

    @QueryMapping
    fun splitHistoryOf(@Argument symbol: String, @Argument exchange: String): List<StockSplits> {
        val stockSymbols = stockSymbolService.findStockSymbol(symbol, null, exchange, null)
        if (stockSymbols.isEmpty()) return listOf()
        val stockSymbol = stockSymbols.first()
        return stockHistoricalDataService.splitHistory(stockSymbol)
    }

    @QueryMapping
    fun dividendHistoryOf(@Argument symbol: String, @Argument exchange: String): List<StockDividends> {
        val stockSymbols = stockSymbolService.findStockSymbol(symbol, null, exchange, null)
        if (stockSymbols.isEmpty()) return listOf()
        val stockSymbol = stockSymbols.first()
        return stockHistoricalDataService.dividendHistory(stockSymbol)
    }
}