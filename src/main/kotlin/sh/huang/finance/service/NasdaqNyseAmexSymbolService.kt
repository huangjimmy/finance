package sh.huang.finance.service

import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.headers
import io.ktor.client.statement.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import sh.huang.finance.constant.ExchangeConstant
import sh.huang.finance.generated.tables.daos.StockSymbolDao
import sh.huang.finance.generated.tables.pojos.StockSymbol
import sh.huang.finance.service.dto.NasdaqStockDataResponse
import sh.huang.finance.service.dto.NasdaqNyseAmexSymbolDTO

/**
 *
 */
@Service
class NasdaqNyseAmexSymbolService() {

    @Autowired lateinit var httpClient: HttpClient

    @Autowired private lateinit var stockSymbolDao: StockSymbolDao

    suspend fun retrieveSymbols(exchange: String): List<NasdaqNyseAmexSymbolDTO> {
        val url = "https://api.nasdaq.com/api/screener/stocks?tableonly=true&limit=4200&exchange=${exchange}"

        val response = httpClient.get(url) {
            headers {
                append("accept", "application/json, text/plain, */*")
                append("accept-language", "en-US,en;q=0.9")
                append("sec-ch-ua", "\"Not A(Brand\";v=\"99\", \"Google Chrome\";v=\"121\", \"Chromium\";v=\"121\"")
                append("sec-ch-ua-mobile", "?0")
                append("sec-ch-ua-platform", "\"macOS\"")
                append("sec-fetch-dest", "empty")
                append("sec-fetch-mode", "cors")
                append("sec-fetch-site", "same-site")
                append("Referer", "https://www.nasdaq.com/")
                append("Referrer-Policy", "strict-origin-when-cross-origin")
            }
        }

        val json = response.bodyAsText()
        val gson = Gson()
        val nasdaqSymbolsResponse = gson.fromJson(json, NasdaqStockDataResponse::class.java)
        return nasdaqSymbolsResponse.data.table.rows;
    }

    fun saveSymbols(symbols: List<NasdaqNyseAmexSymbolDTO>, exchange: String) {
        for (symbol in symbols){
            val existingSymbols = stockSymbolDao.ctx().selectFrom(sh.huang.finance.generated.tables.StockSymbol.STOCK_SYMBOL).where(
                    sh.huang.finance.generated.tables.StockSymbol.STOCK_SYMBOL.SYMBOL.eq(symbol.symbol).
                    and(sh.huang.finance.generated.tables.StockSymbol.STOCK_SYMBOL.EXCHANGE.eq(exchange))
            ).fetchInto(StockSymbol::class.java)

            val existingSymbol = existingSymbols.find { it.symbol == symbol.symbol && it.exchange == exchange }
            when(existingSymbol){
                null -> {
                    val stockSymbol = StockSymbol()
                    stockSymbol.symbol = symbol.symbol
                    stockSymbol.name = symbol.name
                    stockSymbol.exchange = exchange
                    stockSymbolDao.insert(stockSymbol)
                }
                else -> {
                    if (existingSymbol.name != symbol.name){
                        existingSymbol.name = symbol.name
                        stockSymbolDao.update(existingSymbol)
                    }
                }
            }
        }
    }
}