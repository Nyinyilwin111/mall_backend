package com.sein_gar_har.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvatarResponse {
    private boolean success;
    private String message;
    private Map<String, String> data;
    private String timestamp;
}