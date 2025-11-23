package com.sein_gar_har.controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sms")
public class SmsController {

    @GetMapping("/test")
    public String test() {
        return "Push service is running!";
    }
}