package com.nitj.billing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class BillingEngineApplication {
    public static void main(String[] args){
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(BillingEngineApplication.class,args);
    }
}
