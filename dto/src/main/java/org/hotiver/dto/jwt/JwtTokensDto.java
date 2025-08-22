package org.hotiver.dto.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
public class JwtTokensDto {
    private String refreshToken;
    private String accessToken;

    public JwtTokensDto(String refreshToken, String accessToken) {
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }
}
