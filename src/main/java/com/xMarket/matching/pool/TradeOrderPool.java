package com.xMarket.matching.pool;

import com.xMarket.matching.Model.MtradeOrder;

public class TradeOrderPool {
    //最小池容量
    private static int MIN_POOL_SIZE;
    //对象数组
    private MtradeOrder[] slotArray = null;
    //已用数量
    private int usedCnt = 0;
    //对象池
    private int poolSize = 0;

    //构造函数
    public TradeOrderPool(int size){
        if(size < MIN_POOL_SIZE)
            size = MIN_POOL_SIZE;
        slotArray = new MtradeOrder[size];
        for(int i = 0;i < size; i++){
            MtradeOrder item = new MtradeOrder();
            slotArray[i] = item;
        }
        poolSize = size;
    }

    //最大容量
    public int maxCapacity(){
        return poolSize;
    }

    //获取对象
    public MtradeOrder getObj(){
        if(usedCnt >= poolSize)
            return null;
        else
            return slotArray[usedCnt++];
    }

    //已经使用的数量
    public int getUsedCnt(){
        return usedCnt;
    }
}
