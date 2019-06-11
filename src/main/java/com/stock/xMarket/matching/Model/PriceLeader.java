/**
 *
 * 订单档位类
 * 用于存储某一股票的某一价格的订单队列
 *
 */
package com.stock.xMarket.matching.Model;


import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class PriceLeader implements WithId {
    //买卖标志
    private int isbuy;
    //对象池id
    private int poolId;
    //优先级
    private double prior;
    //档位价格
    private double price;
    //总委托量
    private int accumQty;
    //订单队列
    private Queue<Morder> orderlist = new PriorityQueue<Morder>(new Comparator<Morder>() {
        public int compare(Morder o1, Morder o2) {
            if (o1.getDate().after(o2.getDate()))
                return 1;
            else
                return -1;
        }
    });

    public String toString() {
        String string = "";
        string = string + price + " " + accumQty;
        return string;
    }
    public double getPrior() {
        return prior;
    }

    public void setPrior(double prior) {
        this.prior = prior;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double d) {
        this.price = d;
    }

    public Queue<Morder> getOrderlist() {
        return orderlist;
    }

    public void setOrderlist(Queue<Morder> orderlist) {
        this.orderlist = orderlist;
    }

    public int getIsbuy() {
        return isbuy;
    }

    public void setIsbuy(int isbuy) {
        this.isbuy = isbuy;
    }

    @Override
    public int getPoolId() {
        return this.poolId;
    }

    @Override
    public void setPoolId(int id) {
        this.poolId = id;
    }

    public int getAccumQty() {
        return accumQty;
    }

    public void setAccumQty(int accumQty) {
        this.accumQty = accumQty;
    }

}