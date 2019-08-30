package com.xMarket.matching.pool;

import java.lang.reflect.Array;
import java.util.LinkedList;

import com.xMarket.matching.Model.PriceLeader;
import com.xMarket.matching.Model.WithId;

public final class RecyclablePool {

    //
    private static int MIN_POOL_SIZE = 100;

    //对象数组
    private PriceLeader slotArray[] = null;
    //使用标志
    private boolean useflagArray[] = null;
    //
    private LinkedList<PriceLeader> freeList = null;
    //
    private long poolSize = 0;

    //构造函数
    public RecyclablePool(int size)
            throws InstantiationException,IllegalAccessException {
        if(size < MIN_POOL_SIZE)
            size = MIN_POOL_SIZE;
        slotArray = new PriceLeader[size];
        useflagArray = new boolean[size];
        freeList = new LinkedList<PriceLeader>();

        for(int i = 0; i < size; i++)
        {
            PriceLeader item = new PriceLeader();
            item.setPoolId(i);
            slotArray[i] = item;
            useflagArray[i] = false;
            freeList.add(item);
        }

        poolSize = size;
    }

    //最大容量
    public int maxCapacity() {
        return this.slotArray.length;
    }

    //获取对象
    public PriceLeader getObj(){
        if(freeList.size() > 0) {
            PriceLeader obj = freeList.poll();
            if(obj != null) {
                int id = obj.getPoolId();
                useflagArray[id] = true;
            }
            return obj;
        }
        else
            return null;
    }

    //返还对象
    public void putObj(PriceLeader obj) {
        if(obj != null) {
            int id = obj.getPoolId();
            if(0 <= id && id < poolSize) {
                if(useflagArray[id]) {
                    freeList.add(obj);
                    useflagArray[id] = false;
                }
            }
        }
    }

    //查找指定对象
    public PriceLeader findObj(int id) {
        if(0 <= id && id < useflagArray.length) {
            if(useflagArray[id])
                return slotArray[id];
        }
        return null;
    }
}