package com.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class MallApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(MallApplication.class, args);
        System.out.println("this is main");
	}
    
}
