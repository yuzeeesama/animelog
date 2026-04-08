package com.animelog.animelogserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.animelog.animelogserver.mapper")
public class AnimeLogServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnimeLogServerApplication.class, args);
    }

}
