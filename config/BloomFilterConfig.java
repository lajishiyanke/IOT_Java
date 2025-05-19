package com.iot.platform.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BloomFilterConfig {
    
    @Bean
    public RBloomFilter<String> userBloomFilter(RedissonClient redissonClient) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter("userBloomFilter");
        // 预计元素数量为10000，误判率为0.01
        bloomFilter.tryInit(10000L, 0.01);
        return bloomFilter;
    }
} 