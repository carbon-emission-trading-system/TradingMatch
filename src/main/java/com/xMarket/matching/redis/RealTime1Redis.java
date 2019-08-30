package com.xMarket.matching.redis;

import org.springframework.stereotype.Repository;

import com.xMarket.matching.Model.MtradeOrder;
import com.xMarket.model.RealTime1;

@Repository
public class RealTime1Redis extends BaseRedis<RealTime1>{
	
	private static final String REDIS_KEY = "com.xMarket.redis.RealTime1Redis";

    @Override
    public String getRedisKey() {
        return REDIS_KEY; 
    }

}
