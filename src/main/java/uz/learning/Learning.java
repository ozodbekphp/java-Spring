package uz.learning;

import java.util.TimeZone;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;


@SpringBootApplication 
public class Learning{
    public static void main(String[] args){
        TimeZone.setDefault(
            TimeZone.getTimeZone(
                System.getenv().getOrDefault("APP_TIMEZONE", "Asia/Tashkent")
            )
        );

        SpringApplication.run(Learning.class, args);
    }
}