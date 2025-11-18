package com.sein_gar_har.config;

public class JwtConstants {

    private JwtConstants() {}

    public static final String TOKEN_HEADER = "Authorization";
    public static final String EMAIL = "email";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final long ACCESS_TOKEN_VALIDITY = 60 * 60 * 1000L;
    public static final String ISSUER = "sein-gar-har-backend";
    public static final String AUTHORITIES = "authorities";
    public static final String SECRET_KEY = "f74463f962b6695f98ad10c04084383f6b60bff0d52330285e3fbf934ddb5fc0ba5148ddf71d83d22096b62f24c55cd29749bb4f23bf5b2cc6c8dad0f44e6033";
}
