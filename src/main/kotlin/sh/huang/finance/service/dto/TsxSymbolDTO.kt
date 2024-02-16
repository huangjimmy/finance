package sh.huang.finance.service.dto

import com.google.gson.annotations.SerializedName

data class TsxSymbolResponseData(
        @SerializedName("last_updated")
        val lastUpdated: Long,

        val length: Int,

        val results: List<TsxSymbolResult>,

        @SerializedName("http_status_code")
        val httpStatusCode: Int
)

data class TsxSymbolResult(
        val symbol: String,
        val name: String,

        val instruments: List<TsxSymbolDTO>
)

data class TsxSymbolDTO(
        val symbol: String,
        val name: String
)