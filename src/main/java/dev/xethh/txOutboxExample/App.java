package dev.xethh.txOutboxExample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@EnableWebMvc
@EnableScheduling
@EnableAsync
public class App 
{
    public static void main( String[] args)
    {
        SpringApplication.run(App.class);
    }
}
