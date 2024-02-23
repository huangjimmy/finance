package sh.huang.finance

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import sh.huang.finance.constant.ExchangeConstant
import sh.huang.finance.dataproviders.eodhd.EodhdApiClient
import sh.huang.finance.generated.tables.daos.StockSymbolDao
import sh.huang.finance.job.StockSymbolSyncJob
import sh.huang.finance.service.NasdaqNyseAmexSymbolService
import sh.huang.finance.service.StockHistoricalDataService
import java.io.File

@SpringBootTest
class FinanceApplicationTests {

	@Autowired
	private lateinit var nasdaqNyseAmexSymbolService: NasdaqNyseAmexSymbolService
	@Autowired
	private lateinit var stockSymbolDao: StockSymbolDao
	@Autowired
	private lateinit var stockSymbolSyncJob: StockSymbolSyncJob
	@Autowired
	private lateinit var eodhdApiClient: EodhdApiClient
	@Autowired
	private lateinit var stockHistoricalDataService: StockHistoricalDataService

	private val mockEngine = MockEngine { request ->
		when (request.url.parameters["exchange"]) {
			ExchangeConstant.NASDAQ ->
				respond(
						content = ByteReadChannel(File("scripts/nasdaq.json").readText()),
						status = HttpStatusCode.OK,
						headers = headersOf(HttpHeaders.ContentType, "application/json")
				)
			ExchangeConstant.NYSE ->
				respond(
						content = ByteReadChannel(File("scripts/nyse.json").readText()),
						status = HttpStatusCode.OK,
						headers = headersOf(HttpHeaders.ContentType, "application/json")
				)
			else -> {
				val ticker = request.url.pathSegments.last()

				if (arrayOf("MCD.US", "AAPL.US", "AMZN.US", "MSFT.US").contains(ticker)){
					respond(
							content = ByteReadChannel(File("data/${ticker}.json").readText()),
							status = HttpStatusCode.OK,
							headers = headersOf(HttpHeaders.ContentType, "application/json")
					)
				}
				else{
					respond(content = "Forbidden", status = HttpStatusCode.Forbidden)
				}
			}
		}
	}

	@BeforeEach
	fun beforeEach() {
		nasdaqNyseAmexSymbolService.httpClient = HttpClient(mockEngine)
		eodhdApiClient.httpClient = HttpClient(mockEngine)
	}

	@Test
	fun contextLoads() {
	}

	@Test
	fun nyseSymbolJob() {
		stockSymbolSyncJob.syncNyseSymbol()
		val nyseSymbols = stockSymbolDao.fetchByExchange(ExchangeConstant.NYSE)
		assert(nyseSymbols.size == 2815)
		assert(nyseSymbols.all { it.exchange == ExchangeConstant.NYSE })
	}

	@Test
	fun nasdaqSymbolJob() {
		stockSymbolSyncJob.syncNasdaqSymbol()
		val nasdaqSymbols = stockSymbolDao.fetchByExchange(ExchangeConstant.NASDAQ)
		assert(nasdaqSymbols.size == 4110)
		assert(nasdaqSymbols.all { it.exchange == ExchangeConstant.NASDAQ })

		stockSymbolSyncJob.syncNasdaqSymbol()
		val nasdaqSymbols2nd = stockSymbolDao.fetchByExchange(ExchangeConstant.NASDAQ)
		assert(nasdaqSymbols.size == nasdaqSymbols2nd.size)
	}

	@Test
	fun historicalPriceMCD(){
		stockSymbolSyncJob.syncNyseSymbol()
		stockHistoricalDataService.syncHistoricalPriceMCDUS()
		val mcdPrices = stockHistoricalDataService.historicalPriceMCDUS()
		assert(mcdPrices.size > 14000)
	}

	@Test
	fun eodhdClientSuccess(){
		val exchange = "US"
		val successTickrs = arrayOf("MCD", "AAPL", "AMZN", "MSFT")
		val failedTickers = arrayOf("SPY")
		runBlocking {
			val (successFetches, failedFetches) = arrayOf(successTickrs, failedTickers).map{ tickers ->
				tickers.map { symbol ->
					async {
						try {
							symbol to arrayOf(eodhdApiClient.getHistoricalData(symbol, exchange), null)
						} catch (e: Exception) {
							symbol to arrayOf(null, e)
						}
					}
				}
			}

			val successPrices = successFetches.awaitAll()
			val failedPrices = failedFetches.awaitAll()
			assertAll(*successPrices.map { pair -> {
					val (result, exception) = pair.second
					val ticker = pair.first
					assert(successTickrs.contains(ticker))
					assert(result != null) { "expecting non null result for $ticker but received null with $exception" }
					assert(exception == null) { "expecting no exception for $ticker but received $exception" }
				}
			}.toTypedArray())
			assertAll(*failedPrices.map { pair -> {
				val (result, exception) = pair.second
				val ticker = pair.first
				assert(failedTickers.contains(ticker))
				assert(result == null) { "expecting null result for $ticker but received $result" }
				assert(exception != null) { "expecting exception for $ticker but received null" }
			}
			}.toTypedArray())
		}
	}
}
