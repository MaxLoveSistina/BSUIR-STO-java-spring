package com.sto.autoservice.config;

import com.sto.autoservice.core.pricing.PriceCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public PriceCalculator priceCalculator() {
        return new PriceCalculator();
    }
}
