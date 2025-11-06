package com.spring.Exceptions;

import java.time.LocalDateTime;

public record ErrorDetails(String error, String message, LocalDateTime timeStamp) {
}
