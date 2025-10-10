package com.mms;

import com.mms.service.ComponentCacheService;
import com.mms.service.FastenerCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class MmsDecomposorApplication implements CommandLineRunner {

    private final ComponentCacheService componentCacheService;
    private final FastenerCacheService fastenerCacheService;

    public static void main(String[] args) {
        SpringApplication.run(MmsDecomposorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("应用启动完成，开始初始化缓存...");
        
        // 1. 初始化零部件缓存
        try {
            int componentCachedCount = componentCacheService.initializeCache();
            if (componentCachedCount > 0) {
                log.info("零部件缓存初始化成功，共缓存 {} 个零部件", componentCachedCount);
            } else {
                log.warn("零部件缓存初始化失败或Redis不可用");
            }
        } catch (Exception e) {
            log.error("零部件缓存初始化过程中发生错误: {}", e.getMessage(), e);
        }
        
        // 2. 初始化紧固件缓存
        try {
            int fastenerCachedCount = fastenerCacheService.initializeFastenerCache();
            if (fastenerCachedCount > 0) {
                log.info("紧固件缓存初始化成功，共缓存 {} 个紧固件状态", fastenerCachedCount);
            } else {
                log.warn("紧固件缓存初始化失败或Redis不可用");
            }
        } catch (Exception e) {
            log.error("紧固件缓存初始化过程中发生错误: {}", e.getMessage(), e);
        }
        
        log.info("所有缓存初始化完成");
    }
}
