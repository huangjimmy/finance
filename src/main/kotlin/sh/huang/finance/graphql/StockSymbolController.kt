package sh.huang.finance.graphql

import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import sh.huang.finance.constant.ExchangeConstant
import sh.huang.finance.generated.tables.pojos.StockSymbol
import sh.huang.finance.service.NasdaqNyseAmexSymbolService
import sh.huang.finance.service.StockSymbolService
import sh.huang.finance.service.TsxSymbolService

@Controller
class StockSymbolController {

    @Autowired
    private lateinit var stockSymbolService: StockSymbolService
    @Autowired
    private lateinit var nasdaqNyseAmexSymbolService: NasdaqNyseAmexSymbolService
    @Autowired
    private lateinit var tsxSymbolService: TsxSymbolService

    @QueryMapping
    fun symbolById(@Argument id: Long): StockSymbol? {
        val symbol = stockSymbolService.findStockSymbol(id)
        return symbol
    }

    @QueryMapping
    fun allSymbols(): List<StockSymbol>? {
        val symbols = stockSymbolService.findStockSymbol(null, null, null, null)
        return symbols
    }

    @QueryMapping
    fun dualListedSymbols(): List<StockSymbol>? {
        val symbols = stockSymbolService.dualListedStockSymbols()
        return symbols
    }

    @QueryMapping
    fun symbolByPrefix(@Argument prefix: String): List<StockSymbol> {
        val symbols = stockSymbolService.findStockSymbol(null, prefix, null, null)
        return symbols
    }

    @QueryMapping
    fun symbolByName(@Argument name: String): List<StockSymbol> {
        val symbols = stockSymbolService.findStockSymbol(null, null, null, name)
        return symbols
    }

    @QueryMapping
    fun symbolBySymbol(@Argument symbol: String): List<StockSymbol> {
        val symbols = stockSymbolService.findStockSymbol(symbol, null, null, null)
        return symbols
    }

    @QueryMapping
    fun symbolByExchange(@Argument exchange: String): List<StockSymbol> {
        val symbols = stockSymbolService.findStockSymbol(null, null, exchange, null)
        return symbols
    }

    @MutationMapping
    fun syncExchangeSymbols(@Argument exchange: String): List<StockSymbol> {
        if (arrayOf(ExchangeConstant.NYSE, ExchangeConstant.NASDAQ, ExchangeConstant.AMEX).contains(exchange)) {
            runBlocking {
                with(nasdaqNyseAmexSymbolService) {
                    retrieveSymbols(exchange).let { saveSymbols(it, exchange) }
                }
            }
            return stockSymbolService.findStockSymbol(null, null, exchange, null)
        }
        else if(exchange == ExchangeConstant.TSX) {
            with(tsxSymbolService) {
                runBlocking {
                    retrieveSymbols().let { saveSymbols(it) }
                }
            }
            return stockSymbolService.findStockSymbol(null, null, exchange, null)
        }
        else return listOf()
    }
}