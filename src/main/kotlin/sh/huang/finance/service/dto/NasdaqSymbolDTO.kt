package sh.huang.finance.service.dto

import com.google.gson.annotations.SerializedName

data class NasdaqStockDataResponse(
        @SerializedName("data") val data: NasdaqStockData,
        @SerializedName("message") val message: String?,
        @SerializedName("status") val status: NasdaqStockDataResponseStatus
)

data class NasdaqStockData(
        @SerializedName("filters") val filters: Any?, // Change the type to the actual type if known
        @SerializedName("table") val table: NasdaqStockTable,
        @SerializedName("totalrecords") val totalRecords: Int,
        @SerializedName("asof") val asOf: String?
)

data class NasdaqStockTable(
        @SerializedName("asOf") val asOf: String?,
        @SerializedName("headers") val headers: Headers,
        @SerializedName("rows") val rows: List<NasdaqNyseAmexSymbolDTO>
)

data class Headers(
        @SerializedName("symbol") val symbol: String,
        @SerializedName("name") val name: String,
        @SerializedName("lastsale") val lastSale: String,
        @SerializedName("netchange") val netChange: String,
        @SerializedName("pctchange") val pctChange: String,
        @SerializedName("marketCap") val marketCap: String
)

data class NasdaqNyseAmexSymbolDTO(
        @SerializedName("symbol") val symbol: String,
        @SerializedName("name") val name: String,
        @SerializedName("lastsale") val lastSale: String,
        @SerializedName("netchange") val netChange: String,
        @SerializedName("pctchange") val pctChange: String,
        @SerializedName("marketCap") val marketCap: String,
        @SerializedName("url") val url: String
)

data class NasdaqStockDataResponseStatus(
        @SerializedName("rCode") val rCode: Int,
        @SerializedName("bCodeMessage") val bCodeMessage: Any?, // Change the type to the actual type if known
        @SerializedName("developerMessage") val developerMessage: Any? // Change the type to the actual type if known
)
