package com.stock.xMarket.matching.redis;

import org.springframework.stereotype.Repository;

import com.stock.xMarket.matching.Model.MtradeOrder;
import com.stock.xMarket.model.RealTime1;

@Repository
public class RealTime1Redis extends BaseRedis<RealTime1>{
	
	private static final String REDIS_KEY = "com.stock.xMarket.redis.RealTime1Redis";

    @Override
    public String getRedisKey() {
        return REDIS_KEY; 
    }

}
