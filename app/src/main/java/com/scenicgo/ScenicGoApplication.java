package com.scenicgo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 游小景 · 景区分时预约与电子导览系统
 */
@MapperScan("com.scenicgo.**.mapper")
@SpringBootApplication
public class ScenicGoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScenicGoApplication.class, args);
    }
}
