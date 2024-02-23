package sh.huang.finance.dataproviders.yfinance

import com.google.gson.annotations.SerializedName
import java.util.*

data class YahooFinanceStockData(
    val dividends: List<Dividend>,
    val history: List<History>,
    val splits: List<Split>
)

data class Dividend(
    val dividend: Double,
    @SerializedName("effective_date") val effectiveDate: Date
)

data class History(
    @SerializedName("adjusted_close") val adjustedClose: Double,
    val close: Double,
    val high: Double,
    val low: Double,
    val open: Double,
    @SerializedName("trading_day") val tradingDay: Date,
    val volume: Int
)

data class Split(
    val split: Double,
    @SerializedName("trading_day") val tradingDay: Date
)