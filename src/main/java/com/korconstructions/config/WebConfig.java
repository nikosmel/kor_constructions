package com.korconstructions.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.floor-dir:/Users/elenikorovesi/Downloads/korConstructions/uploads/floors}")
    private String floorUploadDir;

    @Value("${app.upload.dir:/Users/elenikorovesi/Downloads/korConstructions/uploads/receipts}")
    private String receiptUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve floor images from /uploads/floors/
        registry.addResourceHandler("/uploads/floors/**")
                .addResourceLocations("file:" + floorUploadDir + "/");

        // Serve receipt images from /uploads/receipts/
        registry.addResourceHandler("/uploads/receipts/**")
                .addResourceLocations("file:" + receiptUploadDir + "/");
    }
}
