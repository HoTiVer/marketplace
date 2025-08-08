package org.hotiver.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SecurityInfoDto {
    private Boolean isTwoFactorEnable;
}
