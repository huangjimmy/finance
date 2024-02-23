package sh.huang.finance.dataproviders.eodhd

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import sh.huang.finance.constant.ExchangeConstant

@Component
class EodhdApiClient {
    @Autowired
    lateinit var httpClient: HttpClient

    @Value("\${eodhd.api.key}")
    lateinit var eodhdApiKey: String
    private fun exchangeEodhd(exchange: String): String {
        return when(exchange){
            ExchangeConstant.NYSE -> "US"
            ExchangeConstant.NASDAQ -> "US"
            ExchangeConstant.AMEX -> "US"
            else -> exchange
        }
    }

    private fun apiToken(ticker: String): String {
        return when(ticker) {
            "MCD.US" -> "demo"
            else -> eodhdApiKey
        }
    }

    suspend fun getHistoricalData(symbol: String, exchange: String): List<EodhdStockDailyData> {
        val ticker = "${symbol}.${exchangeEodhd(exchange)}"
        val url = "https://eodhd.com/api/eod/${ticker}?period=d&api_token=${apiToken(ticker)}&fmt=json"

        val response = httpClient.get(url) {
            headers {
                append("accept", "application/json, text/plain, */*")
                append("accept-language", "en-US,en;q=0.9")
                append("sec-ch-ua", "\"Not A(Brand\";v=\"99\", \"Google Chrome\";v=\"121\", \"Chromium\";v=\"121\"")
                append("sec-ch-ua-mobile", "?0")
                append("sec-ch-ua-platform", "\"macOS\"")
                append("cache-control", "max-age=0")
                append("sec-fetch-dest", "empty")
                append("sec-fetch-mode", "cors")
                append("sec-fetch-site", "same-site")
                append("Referer", "https://eodhd.com/")
                append("Referrer-Policy", "strict-origin-when-cross-origin")
            }
        }
        val json = response.bodyAsText()
        val gson = Gson()
        val stockListType = object : TypeToken<List<EodhdStockDailyData>>() {}.type
        val dailyData: List<EodhdStockDailyData> = gson.fromJson(json, stockListType)
        return dailyData
    }
}