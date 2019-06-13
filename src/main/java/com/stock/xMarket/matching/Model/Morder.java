/**
 *
 * 委托类
 * 用于存储委托信息、进行交易撮合
 *
 */
package com.stock.xMarket.matching.Model;

import com.alibaba.fastjson.JSONObject;
import java.util.Date;

public class Morder {
    //订单id
    private long order_id;
    //股票id
    private int stock_id;
    //买卖标志
    private int type;
    //时间
    private Date date;
    private int owner;
    //委托类型 限价委托
    //         市价委托
    private int trade_straregy;
    //委托数量
    private int order_amount;
    //委托价格
    private double order_price;
    //剩余数量
    private int remQty;
    //撤单标志
    private boolean delflg;
    public Morder() {

    }
    
    public Morder(JSONObject order) {
        this.order_id = order.getLong("orderId");
        this.owner=order.getInteger("userId");
        this.stock_id = order.getInteger("stockId");
        this.date = new Date();
        this.type = order.getInteger("type");
        this.trade_straregy = order.getInteger("tradeStraregy");
        this.order_amount = order.getInteger("orderAmount");
        this.order_price = order.getDouble("orderPrice");
        this.remQty = this.order_amount;
        this.delflg = false;
        //this.owner = order.getInteger("owner");
    }
    public Morder(long order_id, int stock_id, int type,

    		int trade_straregy, int order_amount, float order_price){
        this.order_id = order_id;
        this.stock_id = stock_id;
        this.type = type;
        this.date = new Date();
        this.trade_straregy = trade_straregy;
        this.order_amount = order_amount;
        this.order_price = order_price;
        this.remQty = order_amount;
        this.delflg = false;
    }

    public String toString() {
    	return "" + order_amount + " " + type + " " + order_price;
    }
    
   
    public long getOrder_id() {
		return order_id;
	}

	public void setOrder_id(long order_id) {
		this.order_id = order_id;
	}

	public int getStock_id() {
        return stock_id;
    }

    public void setStock_id(int stock_id) {
        this.stock_id = stock_id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getTrade_straregy() {
        return trade_straregy;
    }

    public void setTrade_straregy(int trade_straregy) {
        this.trade_straregy = trade_straregy;
    }

    public int getOrder_amount() {
        return order_amount;
    }

    public void setOrder_amount(int order_amount) {
        this.order_amount = order_amount;
    }


    public double getOrder_price() {
        return order_price;
    }

    public void setOrder_price(double d) {
        this.order_price = d;
    }

    public int getRemQty() {
        return remQty;
    }

    public void setRemQty(int remQty) {
        this.remQty = remQty;
    }

    public boolean isDelflg() {
        return delflg;
    }

    public void setDelflg(boolean delflg) {
        this.delflg = delflg;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }
}