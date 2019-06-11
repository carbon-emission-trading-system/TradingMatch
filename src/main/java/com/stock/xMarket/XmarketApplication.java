package com.stock.xMarket;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class XmarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(XmarketApplication.class, args);
    }
}
