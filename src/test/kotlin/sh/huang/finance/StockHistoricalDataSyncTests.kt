package sh.huang.finance

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import sh.huang.finance.constant.ExchangeConstant
import sh.huang.finance.dataproviders.yfinance.YahooFinanceClient
import sh.huang.finance.service.NasdaqNyseAmexSymbolService
import sh.huang.finance.service.StockHistoricalDataService
import sh.huang.finance.service.StockSymbolService
import sh.huang.finance.service.dto.NasdaqNyseAmexSymbolDTO
import sh.huang.finance.service.dto.NyseSymbolDTO
import java.io.File

@SpringBootTest
class StockHistoricalDataSyncTests {
    @Autowired
    private lateinit var yahooFinanceClient: YahooFinanceClient
    @Autowired
    private lateinit var stockHistoricalDataService: StockHistoricalDataService
    @Autowired
    private lateinit var stockSymbolService: StockSymbolService
    @Autowired
    private lateinit var nasdaqNyseAmexSymbolService: NasdaqNyseAmexSymbolService

    private val mockEngine = MockEngine { _ ->
        respond(content = ByteReadChannel(File("data/MCD.json").readText()),
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }

    @BeforeEach
    fun beforeEach() {
        yahooFinanceClient.httpClient = HttpClient(mockEngine)
        val mcd = stockSymbolService.findStockSymbol("MCD", null, ExchangeConstant.NYSE, null)
        if (mcd.isEmpty()) {
            val symbol = NasdaqNyseAmexSymbolDTO(symbol = "MCD", name = "MacDonald", url = "", lastSale = "", marketCap = "", pctChange = "", netChange = "");
            nasdaqNyseAmexSymbolService.saveSymbols(listOf(symbol), ExchangeConstant.NYSE)
        }
    }
    @Test
    fun historyTest() {
        val mcd = stockSymbolService.findStockSymbol("MCD", null, ExchangeConstant.NYSE, null)
        assert(mcd.size == 1) { "expect mcd.size = 1 but received ${mcd.size}" }
        stockHistoricalDataService.syncHistoricalYFinance(mcd.first())
        val historyPrices = stockHistoricalDataService.historicalPrice(mcd.first())
        assert(historyPrices.size > 14000) { "expect historyPrices.size > 14000 but received ${historyPrices.size}" }
        val splits = stockHistoricalDataService.splitHistory(mcd.first())
        assert(splits.size > 1) { "expect splits.size > 1 but received ${splits.size}" }
        val dividends = stockHistoricalDataService.dividendHistory(mcd.first())
        assert(dividends.size > 1) { "expect dividends.size > 1 but received ${dividends.size}" }
    }
}