package com.sein_gar_har.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordResponse {
    private boolean success;
    private String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public static ChangePasswordResponse success(String message) {
        return new ChangePasswordResponse(true, message, LocalDateTime.now());
    }

    public static ChangePasswordResponse error(String message) {
        return new ChangePasswordResponse(false, message, LocalDateTime.now());
    }
}