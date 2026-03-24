package com.putl.interactionservice.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.putl.interactionservice.mapper")
public class MybatisPlusConfig {
}
