package cn.edu.xmu.pes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PublicExamServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PublicExamServerApplication.class, args);
    }

}
