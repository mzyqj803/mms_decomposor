package com.mms;

import com.mms.service.ComponentCacheService;
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

    public static void main(String[] args) {
        SpringApplication.run(MmsDecomposorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("应用启动完成，开始初始化零部件缓存...");
        
        try {
            int cachedCount = componentCacheService.initializeCache();
            if (cachedCount > 0) {
                log.info("零部件缓存初始化成功，共缓存 {} 个零部件", cachedCount);
            } else {
                log.warn("零部件缓存初始化失败或Redis不可用");
            }
        } catch (Exception e) {
            log.error("零部件缓存初始化过程中发生错误: {}", e.getMessage(), e);
        }
    }
}
