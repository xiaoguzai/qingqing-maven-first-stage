package com.lantu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;


@Configuration
public class MyCorsConfig {

    @Bean
    public CorsFilter corsFilter(){
        System.out.println("corsFilter");
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:8888");
        //config.addAllowedOrigin("*");
        //config.addAllowedOriginPattern("*");
        //如果是多个url可能需要调用多次
        config.setAllowCredentials(true);
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**",config);

        return new CorsFilter(configurationSource);
    }
}

