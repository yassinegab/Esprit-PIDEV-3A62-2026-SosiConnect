package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SosiApplication {
    private static ApplicationContext context;

    public static void main(String[] args) {
        context = SpringApplication.run(SosiApplication.class, args);
    }

    public static ApplicationContext getContext() {
        return context;
    }

    public static void setContext(ApplicationContext ctx) {
        context = ctx;
    }
}
