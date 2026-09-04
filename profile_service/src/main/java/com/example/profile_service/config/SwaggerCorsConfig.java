package com.example.profile_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Разрешает браузеру ходить в этот сервис (и /v3/api-docs, и реальные /api/v1/**
 * вызовы через "Try it out") со страницы Swagger UI ДРУГОГО сервиса на другом
 * порту (порт = другой origin для браузера, даже на localhost). Только для
 * локальной разработки/тестирования через агрегированный Swagger.
 */
@Configuration
public class SwaggerCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
