package sh.huang.finance.service

import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.headers
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import sh.huang.finance.constant.ExchangeConstant
import sh.huang.finance.generated.tables.StockSymbol.STOCK_SYMBOL
import sh.huang.finance.generated.tables.daos.StockSymbolDao
import sh.huang.finance.generated.tables.pojos.StockSymbol
import sh.huang.finance.service.dto.*

@Service
class TsxSymbolService {
    @Autowired
    lateinit var client: HttpClient

    @Autowired
    private lateinit var stockSymbolDao: StockSymbolDao

    suspend fun retrieveSymbols(prefix: String): List<TsxSymbolResult> {

        val url = "https://www.tsx.com/json/company-directory/search/tsx/${prefix}"

        val response = client.get(url) {
            headers {
                append("accept", "application/json, text/javascript, */*; q=0.01")
                append("accept-language", "en-US,en;q=0.9")
                append("sec-ch-ua", "\"Not A(Brand\";v=\"99\", \"Google Chrome\";v=\"121\", \"Chromium\";v=\"121\"")
                append("sec-ch-ua-mobile", "?0")
                append("sec-ch-ua-platform", "\"macOS\"")
                append("sec-fetch-dest", "empty")
                append("sec-fetch-mode", "cors")
                append("sec-fetch-site", "same-origin")
                append("x-requested-with", "XMLHttpRequest")
                append("Referer", "https://www.tsx.com/listings/listing-with-us/listed-company-directory")
                append("Referrer-Policy", "strict-origin-when-cross-origin")
            }
        }

        val json = response.bodyAsText()
        val gson = Gson()
        val nasdaqSymbolsResponse = gson.fromJson(json, TsxSymbolResponseData::class.java)
        return nasdaqSymbolsResponse.results;
    }

    suspend fun retrieveSymbols(): List<TsxSymbolDTO> {
        val uppercaseLetters = ('A'..'Z').map { it.toString() }.toTypedArray()

        val results = uppercaseLetters.map {
            runBlocking { retrieveSymbols(it).map { it.instruments } }
        }.flatten().flatten()
        return results
    }

    fun saveSymbols(symbolResults: List<TsxSymbolDTO>) {
        for (symbol in symbolResults) {
            val existingSymbols = stockSymbolDao.ctx().selectFrom(STOCK_SYMBOL).where(
                    STOCK_SYMBOL.SYMBOL.eq(symbol.symbol).and(STOCK_SYMBOL.EXCHANGE.eq(ExchangeConstant.TSX))
            ).fetchInto(StockSymbol::class.java)

            val existingSymbol = existingSymbols.find { it.symbol == symbol.symbol && it.exchange == ExchangeConstant.TSX }
            when (existingSymbol) {
                null -> {
                    val stockSymbol = StockSymbol()
                    stockSymbol.symbol = symbol.symbol
                    stockSymbol.name = symbol.name
                    stockSymbol.exchange = ExchangeConstant.TSX
                    stockSymbolDao.insert(stockSymbol)
                }

                else -> {
                    if (existingSymbol.name != symbol.name) {
                        existingSymbol.name = symbol.name
                        stockSymbolDao.update(existingSymbol)
                    }
                }
            }
        }
    }
}