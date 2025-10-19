package org.hotiver.dto.auth;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class AuthDto {
    private Boolean isSuccess;
    private String message;
    private String accessToken;
    private String refreshToken;
    private String redirectUrl;
}
