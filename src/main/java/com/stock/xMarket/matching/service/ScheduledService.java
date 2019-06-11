package com.stock.xMarket.matching.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.stock.xMarket.matching.config.RabbitConfig;
import com.stock.xMarket.matching.utils.RabbitmqUtils;

@Component
public class ScheduledService {
    @Autowired
    private RabbitmqUtils utils;

    @Scheduled(cron = " 0 57 14 ? * MON-FRI")
    @Scheduled(cron =" 0 15 9 ? * MON-FRI")
    public void startAllo() {
        utils.restartMessageListener(RabbitConfig.QUEUE_D);
    }

    @Scheduled(cron = "0 0 15 ? * MON-FRI")
    @Scheduled(cron = "0 25 9 ? * MON-FRI")
    public void stopAllo() {
        utils.stopMessageListener(RabbitConfig.QUEUE_D);
    }

    @Scheduled(cron = "0 0 13 ? * MON-FRI")
    @Scheduled(cron = "0 30 9 ? * MON-FRI")
    public void startCon() {
        utils.restartMessageListener(RabbitConfig.QUEUE_A);
    }

    @Scheduled(cron = "0 30 11 ? * MON-FRI")
    @Scheduled(cron = "0 57 14 ? * MON-FRI")
    public void stopCon() {
        utils.stopMessageListener(RabbitConfig.QUEUE_A);
    }

    @Scheduled(cron = "0 15 9 ? * MON-FRI")
    @Scheduled(cron = "0 30 9 ? * MON-FRI")
    @Scheduled(cron = "0 0 13 ? * MON-FRI")
    public void startDel() {
        utils.restartMessageListener(RabbitConfig.QUEUE_B);
    }

    @Scheduled(cron = "0 20 9 ? * MON-FRI")
    @Scheduled(cron = "0 30 11 ? * MON-FRI")
    @Scheduled(cron = "0 57 14 ? * MON-FRI")
    public void stopDel() {
        utils.stopMessageListener(RabbitConfig.QUEUE_B);
    }

}
