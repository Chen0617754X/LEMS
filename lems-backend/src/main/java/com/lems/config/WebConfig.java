
package com.lems.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    // 注册全局登录拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**");
    }

    // 配置 Vue 前端跨域访问
    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/api/**")
                .allowedOriginPatterns(
                        // 本地开发
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        // Netlify 线上部署
                        "https://*.netlify.app"
                )
                .allowedMethods(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
