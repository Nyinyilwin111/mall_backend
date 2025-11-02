package com.spring.DTO.request;

public record UpdateUserRequestDTO(String email, String password, String fullName) {
}