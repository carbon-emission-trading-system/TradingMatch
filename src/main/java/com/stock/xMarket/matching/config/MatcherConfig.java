package com.stock.xMarket.matching.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.stock.xMarket.matching.Matcher;

@Configuration
public class MatcherConfig {
    @Bean
    Matcher matcher() {
        try {
            return new Matcher(1000, 400000, 200000);
        } catch (InstantiationException e) {
            e.printStackTrace();
            System.out.println("instant");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.out.println("illegal");
        }
        return null;
    }
}
