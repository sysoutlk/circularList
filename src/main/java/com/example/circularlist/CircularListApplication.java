package com.example.circularlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CircularListApplication {

    public static void main(String[] args) {
        SpringApplication.run(CircularListApplication.class, args);
        System.out.println("\n=====================================");
        System.out.println("    循环列表系统已启动(BRPOPLPUSH)");
        System.out.println("    访问: http://localhost:9005");
        System.out.println("=====================================\n");
    }

}
