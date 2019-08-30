package com.xMarket.matching.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.xMarket.matching.Matcher;

@Configuration
public class MatcherConfig {
    @Bean
    Matcher matcher() {
        try {
            return new Matcher(1000, 500000, 500000);
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
