package com.example.springboot.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/admin/login",
                        "/admin/resetPassword",
                        "/stu/login",
                        "/stu/register",
                        "/stu/resetPassword",
                        "/parent/login",
                        "/parent/register",
                        "/parent/resetPassword",
                        "/main/loadIdentity",
                        "/main/loadUserInfo",
                        "/error",
                        "/files/**"
                );
    }
}
