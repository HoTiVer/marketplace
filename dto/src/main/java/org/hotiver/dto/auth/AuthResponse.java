package org.hotiver.dto.auth;


public record AuthResponse(
        String accessToken,
        String refreshToken,
        Long accessTokenLifeTime,
        Long refreshTokenLifeTime
) {}
