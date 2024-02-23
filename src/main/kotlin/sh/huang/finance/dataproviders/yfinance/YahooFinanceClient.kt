package sh.huang.finance.dataproviders.yfinance

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import sh.huang.finance.constant.ExchangeConstant
import sh.huang.finance.generated.tables.daos.YfinanceCacheDao
import sh.huang.finance.generated.tables.pojos.YfinanceCache
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

@Component
class YahooFinanceClient {
    @Autowired
    lateinit var httpClient: HttpClient
    @Autowired
    private lateinit var yfinanceCacheDao: YfinanceCacheDao

    @Value("\${yahoo.finance.url}")
    private lateinit var yfinanceUrl: String

    private fun yahooStockSymbol(symbol: String, exchange: String): String {
        return when(exchange){
            ExchangeConstant.NYSE -> symbol
            ExchangeConstant.NASDAQ -> symbol
            ExchangeConstant.AMEX -> symbol
            ExchangeConstant.TSX -> "${symbol.replace(".", "-")}.TO"
            else -> symbol
        }
    }

    suspend fun getStock(symbol: String, exchange: String): YahooFinanceStockData {
        val ticker = yahooStockSymbol(symbol, exchange)
        val gson = Gson()

        val cache = yfinanceCacheDao.fetchOptionalByTicker(ticker)
        var history: List<History>? = null
        var splits: List<Split>? = null
        var dividends: List<Dividend>? = null

        val historyListType = object : TypeToken<List<History>>() {}.type
        val splitListType = object : TypeToken<List<Split>>() {}.type
        val dividendListType = object : TypeToken<List<Dividend>>() {}.type

        if (cache.isPresent) {
            val cacheContent = cache.get()
            history = gson.fromJson<List<History>>(String(cacheContent.history), historyListType::class.java)
            splits = gson.fromJson<List<Split>>(String(cacheContent.splits), splitListType::class.java)
            dividends = gson.fromJson<List<Dividend>>(String(cacheContent.dividends), dividendListType::class.java)

            if (Instant.now() < cacheContent.expiresat.toInstant(ZoneOffset.UTC)) {
                //return cache
                val yahooData = YahooFinanceStockData(dividends, history, splits)
                return yahooData
            }
        }

        val url = "${yfinanceUrl}/ticker/${ticker}"

        val response = httpClient.get(url)
        val json = response.bodyAsText()
        val yahooData = gson.fromJson(json, YahooFinanceStockData::class.java)

        val currentUtcDateTime = LocalDateTime.now(ZoneId.of("UTC"))
        val estTimeZone = ZoneId.of("America/New_York")
        val estDateTime = currentUtcDateTime.atZone(ZoneId.of("UTC")).withZoneSameInstant(estTimeZone)
        // Set the time to 11:00 PM
        val estDateTimeAt11PM = estDateTime
            .withHour(23).withMinute(0).withSecond(0).withNano(0)
            .toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime()

        if (cache.isPresent) {
            val cacheContent = cache.get()
            cacheContent.expiresat = estDateTimeAt11PM
            if(history == null || yahooData.history.size > history.size) cacheContent.history = gson.toJson(yahooData.history).toByteArray()
            if(splits == null || yahooData.splits.size > splits.size) cacheContent.splits = gson.toJson(yahooData.splits).toByteArray()
            if(dividends == null || yahooData.dividends.size > dividends.size) cacheContent.dividends = gson.toJson(yahooData.dividends).toByteArray()
            yfinanceCacheDao.update(cacheContent)
        }
        else {
            history = yahooData.history
            splits = yahooData.splits
            dividends = yahooData.dividends
            yfinanceCacheDao.insert(YfinanceCache(ticker,
                gson.toJson(history).toByteArray(),
                gson.toJson(splits).toByteArray(),
                gson.toJson(dividends).toByteArray(),
                estDateTimeAt11PM))
        }

        return yahooData
    }
}