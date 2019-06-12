package com.stock.xMarket.matching.Model;


import com.alibaba.fastjson.JSONObject;

import java.util.Date;

public class MtradeOrder {
    //成交单id
    private int tradeOrderId;
    //股票id
    private int stockID;
    //买方委托id
    private int buyOrderId;
    //卖方委托id
    private int sellOrderId;
    //卖方标志
    private boolean sellPoint;
    //买方标志
    private boolean buyPoint;
    //时间
    private Date date;
    //成交额
    private double tradePrice;
    //成交量
    private int exchangeAmount;
    //买卖盘
    private boolean tradeType;
    private int buyerId;
    private int sellerId;

    public String toString() {
        String string = "";
        string = string + stockID + " " + buyOrderId + " " + sellOrderId + " " + sellPoint + " " + buyPoint + " " + tradePrice + " "
                + exchangeAmount;
        return string;
    }
    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tradeOrderId", getTradeOrderId());
        jsonObject.put("stockId", getStockID());
        jsonObject.put("buyOrderId", getBuyOrderId());
        jsonObject.put("sellOrderId", getSellOrderId());
        jsonObject.put("sellPoint", isSellPoint());
        jsonObject.put("buyPoint", isBuyPoint());
        jsonObject.put("date", getDate());
        jsonObject.put("time", new java.sql.Time(getDate().getTime()));
        jsonObject.put("tradePrice", getTradePrice());
        jsonObject.put("exchangeAmount", getExchangeAmount());
        jsonObject.put("tradeType", isTradeType());
        jsonObject.put("buyerId",getBuyerId());
        jsonObject.put("sellerId",getSellerId());
        return jsonObject.toJSONString();
    }

    public int getTradeOrderId() {
        return tradeOrderId;
    }

    public void setTradeOrderId(int tradeOrderId) {
        this.tradeOrderId = tradeOrderId;
    }

    public int getStockID() {
        return stockID;
    }

    public void setStockID(int stockID) {
        this.stockID = stockID;
    }

    public int getBuyOrderId() {
        return buyOrderId;
    }

    public void setBuyOrderId(int buyOrderId) {
        this.buyOrderId = buyOrderId;
    }

    public int getSellOrderId() {
        return sellOrderId;
    }

    public void setSellOrderId(int sellOrderId) {
        this.sellOrderId = sellOrderId;
    }

    public boolean isSellPoint() {
        return sellPoint;
    }

    public void setSellPoint(boolean sellPoint) {
        this.sellPoint = sellPoint;
    }

    public boolean isBuyPoint() {
        return buyPoint;
    }

    public void setBuyPoint(boolean buyPoint) {
        this.buyPoint = buyPoint;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getTradePrice() {
        return tradePrice;
    }

    public void setTradePrice(double tradePrice) {
        this.tradePrice = tradePrice;
    }

    public int getExchangeAmount() {
        return exchangeAmount;
    }

    public void setExchangeAmount(int exchangeAmount) {
        this.exchangeAmount = exchangeAmount;
    }

    public boolean isTradeType() {
        return tradeType;
    }

    public void setTradeType(boolean tradeType) {
        this.tradeType = tradeType;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public int getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
    }
}
