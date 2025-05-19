package com.iot.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@MapperScan("com.iot.platform.mapper")
@EnableTransactionManagement
@EnableScheduling
public class IotPlatformApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(IotPlatformApplication.class, args);
    }
}