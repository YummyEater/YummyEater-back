package com.YammyEater.demo.config;

import com.YammyEater.demo.repository.upload.TempResourceRepository;
import com.YammyEater.demo.service.upload.LocalResourceUploadService;
import com.YammyEater.demo.service.upload.ResourceUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@Profile("upload-local")
public class LocalResourceUploadConfig implements WebMvcConfigurer {
    @Value("${spring.upload.upload_root}")
    private String UPLOAD_ROOT;

    @Value("${spring.upload.resource_host}")
    private String RESOURCE_HOST;

    @Value("${spring.upload.urlPath}")
    private String URL_PATH;

    private final TempResourceRepository tempResourceRepository;

    @Bean
    public ResourceUploadService getLocalResourceUploadService() {
        return new LocalResourceUploadService(UPLOAD_ROOT, RESOURCE_HOST, URL_PATH, tempResourceRepository);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(URL_PATH + "**").addResourceLocations("file:///" + UPLOAD_ROOT);
    }
}
