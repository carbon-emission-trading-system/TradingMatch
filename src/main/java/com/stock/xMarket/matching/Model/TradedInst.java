package com.stock.xMarket.matching.Model;

import javax.persistence.*;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

@Entity
@Table (name = "mstock")
public class TradedInst {
    //股票代码
    @Id
    @Column(name = "stock_id")
    private int stockId;
    //股票简称
    @Column(name = "stock_name")
    private String stockname;
    //前一收盘价
    @Column(name = "past_close_price")
    private Double pastClosePrice;
    @Transient
    private Double openPrice;
    @Transient
    private Double closePrice;
    //最新成交价
    @Transient
    private Double new_price;
    //买入价格队列
    @Transient
    private TreeMap<Double,PriceLeader> buyPrcList;
    //卖出价格队列
    @Transient
    private TreeMap<Double,PriceLeader> sellPrcList;
    @Transient
    private double maxPrice;
    @Transient
    private double minPrice;
    @Transient
    private int tradeVolumn;
    @Transient
    private Double tradeAmount;

    public TradedInst(){

    }

    public TradedInst(int stockId, String stockName, double pastClosePrice)
    {
        this.stockId = stockId;
        this.stockname = stockName;
        this.pastClosePrice = pastClosePrice;
        this.closePrice = 0.0;
        this.openPrice = 0.0;
        this.maxPrice = Double.MIN_VALUE;
        this.minPrice = Double.MAX_VALUE;
        this.new_price = 0.0;
        this.tradeVolumn = 0;
        this.tradeAmount = 0.0;
        buyPrcList = new TreeMap<Double, PriceLeader>();
        sellPrcList = new TreeMap<Double, PriceLeader>();
    }
    // 获得本方价位列表树
    public final TreeMap<Double, PriceLeader> getPrcList(int isBuy)
    {
        if(isBuy == 0)
        {
            return buyPrcList;
        }
        else
        {
            return sellPrcList;
        }
    }

    // 获得对手方价位列表树
    public final TreeMap<Double, PriceLeader> getPeerPrcTree(int isBuy)
    {
        if(isBuy != 0)
        {
            return buyPrcList;
        }
        else
        {
            return sellPrcList;
        }
    }

    // 向对应价位列表树上添加一个价位
    public final void addtoPrcList(boolean isBuy, PriceLeader prcLdr)
    {
        if(isBuy)
        {
            buyPrcList.put(prcLdr.getPrior(), prcLdr);
        }
        else
        {
            sellPrcList.put(prcLdr.getPrior(), prcLdr);
        }
    }

    //获取最优对手方价位
    public final Map.Entry<Double, PriceLeader> getBestPeerPrcLdr(int isBuy)
    {
        Map.Entry<Double, PriceLeader> bestPrcLdr = null;
        TreeMap<Double, PriceLeader> prcList = getPeerPrcTree(isBuy);

        bestPrcLdr = prcList.firstEntry();

        return bestPrcLdr;
    }

    public String toString() {
        return stockname + " " + stockId;
    }

    public String toString1() {
        String string = "";
        Iterator<Map.Entry<Double, PriceLeader>> itsB =  buyPrcList.entrySet().iterator();
        while(itsB.hasNext()) {
            Map.Entry<Double, PriceLeader> temp = itsB.next();
            string = string +" " + temp.getValue().getPrice() + " " + temp.getValue().getAccumQty();
        }
        return string;
    }
    public String toString2() {
        String string = "";
        Iterator<Map.Entry<Double, PriceLeader>> itsS = sellPrcList.entrySet().iterator();
        while(itsS.hasNext()) {
            Map.Entry<Double, PriceLeader> temp = itsS.next();
            string = string +" " + temp.getValue().getPrice() + " " + temp.getValue().getAccumQty();
        }
        return string;
    }
    public int getStockId() {
        return stockId;
    }

    public void setStockId(int stockId) {
        this.stockId = stockId;
    }

    public String getStockname() {
        return stockname;
    }

    public void setStockname(String stockname) {
        this.stockname = stockname;
    }

    public Double getNew_price() {
        return new_price;
    }

    public void setNew_price(Double new_price) {
        this.new_price = new_price;
    }

    public Double getPastClosePrice() {
        return pastClosePrice;
    }

    public void setPastClosePrice(Double pastClosePrice) {
        this.pastClosePrice = pastClosePrice;
    }

    public Double getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(Double openPrice) {
        this.openPrice = openPrice;
    }

    public Double getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(Double closePrice) {
        this.closePrice = closePrice;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(double minPrice) {
        this.minPrice = minPrice;
    }

    public int getTradeVolumn() {
        return tradeVolumn;
    }

    public void setTradeVolumn(int tradeVolumn) {
        this.tradeVolumn = tradeVolumn;
    }

    public double getTradeAmount() {
        return tradeAmount;
    }

    public void setTradeAmount(double tradeAmount) {
        this.tradeAmount = tradeAmount;
    }
}