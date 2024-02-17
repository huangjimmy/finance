package sh.huang.finance

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.DependsOn
import org.springframework.core.env.Environment
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.EnabledIf
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
	@Autowired
	private lateinit var environment: Environment

	val nasdaqApiHost = "api.nasdaq.com";
	val nyseHost = "www.nyse.com";

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
				if (request.url.pathSegments.last() == "MCD.US"){
					respond(
							content = ByteReadChannel(File("data/MCD.US.json").readText()),
							status = HttpStatusCode.OK,
							headers = headersOf(HttpHeaders.ContentType, "application/json")
					)
				}
				else{
					respond(content = "")
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
}
