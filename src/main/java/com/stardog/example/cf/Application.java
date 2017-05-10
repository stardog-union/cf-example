package com.stardog.example.cf;

import com.stardog.example.cf.web.StardogController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;

public class Application extends SpringBootServletInitializer {

    private static final Log logger = LogFactory.getLog(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(StardogController.class, args);
    }
}
