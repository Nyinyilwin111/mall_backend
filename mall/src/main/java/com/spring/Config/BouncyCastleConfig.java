package com.spring.Config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.security.Security;

@Configuration
public class BouncyCastleConfig {

    @PostConstruct
    public void init() {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
            System.out.println("✅ BouncyCastle provider registered");
        }
    }
}
