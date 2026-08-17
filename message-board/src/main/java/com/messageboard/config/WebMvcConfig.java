package com.messageboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.io.File;

/**
 * 跨域配置 + 静态资源映射
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private FileUploadConfig fileUploadConfig;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射上传文件目录，使附件可通过 URL 访问
        String uploadPath = fileUploadConfig.getPath();
        if (!uploadPath.endsWith("/")) {
            uploadPath += "/";
        }
        // 转为绝对路径
        File dir = new File(uploadPath);
        String absolutePath = dir.getAbsolutePath();
        if (!absolutePath.endsWith("/")) {
            absolutePath += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath);
    }
}
