package sh.huang.finance.dataproviders.eodhd

import com.google.gson.annotations.SerializedName

data class EodhdStockDailyData(
        @SerializedName("date") val date: String,
        @SerializedName("open") val open: Double,
        @SerializedName("high") val high: Double,
        @SerializedName("low") val low: Double,
        @SerializedName("close") val close: Double,
        @SerializedName("adjusted_close") val adjustedClose: Double,
        @SerializedName("volume") val volume: Long
)