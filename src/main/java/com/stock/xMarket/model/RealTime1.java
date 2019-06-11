package com.stock.xMarket.model;

import java.io.Serializable;
import java.util.List;

public class RealTime1 implements Serializable {
    private int stockId;
    private double lastTradePrice; //最新成交价
    private double openPrice; //今日开盘价
    private double highestPrice; //最高价
    private double lowestPrice; //最低价
    private int volume;//成交量
    private double tradeAmount; //成交额
    private List<Gear> buyOneToFive;//买一买五
    private List<Gear> sellOneToFive;//卖一卖五

 

    public RealTime1(int stockId, double lastTradePrice, double openPrice, double highestPrice, double lowestPrice,
			int volume, double tradeAmount, List<Gear> buyOneToFive, List<Gear> sellOneToFive) {
		super();
		this.stockId = stockId;
		this.lastTradePrice = lastTradePrice;
		this.openPrice = openPrice;
		this.highestPrice = highestPrice;
		this.lowestPrice = lowestPrice;
		this.volume = volume;
		this.tradeAmount = tradeAmount;
		this.buyOneToFive = buyOneToFive;
		this.sellOneToFive = sellOneToFive;
	}

    
    
    
	public RealTime1() {
	}




	public int getStockId() {
		return stockId;
	}




	public void setStockId(int stockId) {
		this.stockId = stockId;
	}




	public double getLastTradePrice() {
		return lastTradePrice;
	}




	public void setLastTradePrice(double lastTradePrice) {
		this.lastTradePrice = lastTradePrice;
	}




	public double getOpenPrice() {
		return openPrice;
	}




	public void setOpenPrice(double openPrice) {
		this.openPrice = openPrice;
	}




	public double getHighestPrice() {
		return highestPrice;
	}




	public void setHighestPrice(double highestPrice) {
		this.highestPrice = highestPrice;
	}




	public double getLowestPrice() {
		return lowestPrice;
	}




	public void setLowestPrice(double lowestPrice) {
		this.lowestPrice = lowestPrice;
	}




	public int getVolume() {
		return volume;
	}




	public void setVolume(int volume) {
		this.volume = volume;
	}




	public double getTradeAmount() {
		return tradeAmount;
	}




	public void setTradeAmount(double tradeAmount) {
		this.tradeAmount = tradeAmount;
	}




	public List<Gear> getBuyOneToFive() {
		return buyOneToFive;
	}




	public void setBuyOneToFive(List<Gear> buyOneToFive) {
		this.buyOneToFive = buyOneToFive;
	}




	public List<Gear> getSellOneToFive() {
		return sellOneToFive;
	}




	public void setSellOneToFive(List<Gear> sellOneToFive) {
		this.sellOneToFive = sellOneToFive;
	}



}
