package com.stock.xMarket.matching.Model;

import java.util.HashMap;

public class TradedInstList {

    private static int INIT_CAPACITY = 100;

    //撮合列表<股票id，股票容器>
    private HashMap<String,TradedInst> list;

    public TradedInstList(){
        list = new HashMap<String, TradedInst>(INIT_CAPACITY);
    }

    public HashMap<String,TradedInst> getList() {
        return this.list;
    }

    //添加股票
    public final void addStock(TradedInst stock) {
        if(stock != null)
            list.put(stock.getStockId(),stock);
    }

    //获取股票
    public final TradedInst getStock(int stockId) {
        return list.get(stockId);
    }
}
