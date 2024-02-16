package sh.huang.finance.service.dto

import com.google.gson.annotations.SerializedName

data class NyseSymbolDTO(
        @SerializedName("total") val total: Int,
        @SerializedName("url") val url: String,
        @SerializedName("exchangeId") val exchangeId: String,
        @SerializedName("instrumentType") val instrumentType: String,
        @SerializedName("symbolTicker") val symbolTicker: String,
        @SerializedName("symbolExchangeTicker") val symbolExchangeTicker: String,
        @SerializedName("normalizedTicker") val normalizedTicker: String,
        @SerializedName("symbolEsignalTicker") val symbolEsignalTicker: String,
        @SerializedName("instrumentName") val instrumentName: String,
        @SerializedName("micCode") val micCode: String
)