package com.stock.xMarket.model;


import java.sql.Date;
import java.sql.Time;

public class Order {
    //订单id
    private int order_id;
    //股票id
    private int stock_id;
    //买卖标志
    private int type;
    //委托人id
    private int user_id;
    //时间
    private Time local_time;
    //日期
    private Date date;
    //委托类型 限价委托
    //         市价委托
    private int trade_straregy;
    //委托数量
    private int order_amount;
    //成交量
    private int exchange_amount;
    //委托价格
    private float order_price;
    //成交均价
    private float exchange_average_price;
    //撤单数量
    private int cancel_number;

    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(int order_id) {
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

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public Time getLocal_time() {
        return local_time;
    }

    public void setLocal_time(Time local_time) {
        this.local_time = local_time;
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

    public int getExchange_amount() {
        return exchange_amount;
    }

    public void setExchange_amount(int exchange_amount) {
        this.exchange_amount = exchange_amount;
    }

    public float getOrder_price() {
        return order_price;
    }

    public void setOrder_price(float order_price) {
        this.order_price = order_price;
    }

    public float getExchange_average_price() {
        return exchange_average_price;
    }

    public void setExchange_average_price(float exchange_average_price) {
        this.exchange_average_price = exchange_average_price;
    }

    public int getCancel_number() {
        return cancel_number;
    }

    public void setCancel_number(int cancel_number) {
        this.cancel_number = cancel_number;
    }
}