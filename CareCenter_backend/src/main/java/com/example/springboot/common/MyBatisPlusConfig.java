package com.example.springboot.common;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class MyBatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
//1. 原有的分页插件（保留不动，保证前端列表不出错）
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
// 2. 乐观锁插件（答辩高光亮点）
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());return interceptor;
    }
}