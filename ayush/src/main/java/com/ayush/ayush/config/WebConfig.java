package com.ayush.ayush.config; // Can be in the existing 'config' package

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // This configuration allows the React frontend to call the backend API
        registry.addMapping("/api/**") // Apply CORS to all endpoints under /api
                .allowedOrigins("http://localhost:5173") // The origin of your React app
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}