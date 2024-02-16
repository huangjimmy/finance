package sh.huang.finance.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import sh.huang.finance.dataproviders.eodhd.EodhdApiClient
import sh.huang.finance.dataproviders.eodhd.EodhdStockDailyData
import sh.huang.finance.generated.tables.pojos.StockSymbol

@Service
class EodhdApiService {
    @Autowired
    private lateinit var eodhdApiClient: EodhdApiClient

    suspend fun historicalPrice(symbol: StockSymbol): List<EodhdStockDailyData> {
        return eodhdApiClient.getHistoricalData(symbol.symbol, symbol.exchange)
    }
}