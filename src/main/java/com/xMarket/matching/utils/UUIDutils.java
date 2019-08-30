package com.xMarket.matching.utils;

import java.util.UUID;

public class UUIDutils {
    public static String getUUID(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static int getGuid(int userId) {


        int now = (int) (System.currentTimeMillis()/1000);
        String info=userId+now+"";
        int id= Integer.parseInt(info);

        return id;
    }

}
