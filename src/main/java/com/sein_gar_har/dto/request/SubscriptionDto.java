package com.sein_gar_har.dto.request;

import lombok.Data;

@Data
public class SubscriptionDto {
    private String endpoint;
    private Keys keys;

    @Data
    public static class Keys {
        private String p256dh;
        private String auth;
    }
}
