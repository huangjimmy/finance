package sh.huang.finance.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import sh.huang.finance.constant.ExchangeConstant
import sh.huang.finance.generated.tables.StockSymbol.STOCK_SYMBOL
import sh.huang.finance.generated.tables.daos.StockSymbolDao
import sh.huang.finance.generated.tables.pojos.StockSymbol

@Service
class StockSymbolService {
    @Autowired
    private lateinit var stockSymbolDao: StockSymbolDao

    fun findStockSymbol(id: Long?): StockSymbol? {
        return stockSymbolDao.findById(id)
    }

    fun findStockSymbol(symbol: String?, prefix: String?, exchange: String?, name: String?): List<StockSymbol> {
        var condition = STOCK_SYMBOL.ID.gt(0)
        symbol?.let {
            condition = condition.and(STOCK_SYMBOL.SYMBOL.eq(symbol));
        }
        prefix?.let {
            condition = condition.and(STOCK_SYMBOL.SYMBOL.startsWith(prefix));
        }
        exchange?.let {
            condition = condition.and(STOCK_SYMBOL.EXCHANGE.eq(exchange));
        }
        name?.let {
            condition = condition.and(STOCK_SYMBOL.NAME.like("%${name}%"));
        }
        return stockSymbolDao.ctx().selectFrom(STOCK_SYMBOL).where(condition)
                .fetchInto(StockSymbol::class.java)
    }

    fun dualListedStockSymbols(): List<StockSymbol> {
        return stockSymbolDao.ctx().selectFrom(STOCK_SYMBOL)
                .where("ID in (SELECT a.id from STOCK_SYMBOL a inner join STOCK_SYMBOL b on a.symbol = b.symbol and a.exchange <> b.exchange)")
                .orderBy(STOCK_SYMBOL.SYMBOL.asc())
                .fetchInto(StockSymbol::class.java)
    }
}