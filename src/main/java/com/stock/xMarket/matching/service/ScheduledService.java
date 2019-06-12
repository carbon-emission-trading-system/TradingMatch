package com.stock.xMarket.matching.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.stock.xMarket.matching.config.RabbitConfig;
import com.stock.xMarket.matching.utils.RabbitmqUtils;

@Component
public class ScheduledService {
    @Autowired
    private RabbitmqUtils utils;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Scheduled(cron = " 0 57 14 ? * MON-FRI")
    @Scheduled(cron =" 0 15 9 ? * MON-FRI")
    public void startAllo() {
        utils.restartMessageListener(RabbitConfig.QUEUE_D);
        logger.info("开启对集合竞价委托队列的监听");
    }

    @Scheduled(cron = "0 0 15 ? * MON-FRI")
    @Scheduled(cron = "0 25 9 ? * MON-FRI")
    public void stopAllo() {
        utils.stopMessageListener(RabbitConfig.QUEUE_D);
        logger.info("关闭对集合竞价委托队列的监听");
    }

    @Scheduled(cron = "0 0 13 ? * MON-FRI")
    @Scheduled(cron = "0 30 9 ? * MON-FRI")
    public void startCon() {
        utils.restartMessageListener(RabbitConfig.QUEUE_A);
        logger.info("开启对连续竞价委托队列的监听");
    }

    @Scheduled(cron = "0 30 11 ? * MON-FRI")
    @Scheduled(cron = "0 57 14 ? * MON-FRI")
    public void stopCon() {
        utils.stopMessageListener(RabbitConfig.QUEUE_A);
        logger.info("关闭对集合竞价委托队列的监听");
    }

    @Scheduled(cron = "0 15 9 ? * MON-FRI")
    @Scheduled(cron = "0 30 9 ? * MON-FRI")
    @Scheduled(cron = "0 0 13 ? * MON-FRI")
    public void startDel() {
        utils.restartMessageListener(RabbitConfig.QUEUE_B);
        logger.info("开启对撤单队列的监听");
    }

    @Scheduled(cron = "0 20 9 ? * MON-FRI")
    @Scheduled(cron = "0 30 11 ? * MON-FRI")
    @Scheduled(cron = "0 57 14 ? * MON-FRI")
    public void stopDel() {
        utils.stopMessageListener(RabbitConfig.QUEUE_B);
        logger.info("关闭对撤单队列的监听");
    }

}
