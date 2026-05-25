package com.syfe.personalfinance.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI personalFinanceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Personal Finance Manager API")
                        .description("Production-quality secure REST API for personal asset logging, custom category mapping, chronological transaction tracking, dynamic savings goal analysis, and monthly/yearly reporting projections.")
                        .version("1.0.0"));
    }
}
