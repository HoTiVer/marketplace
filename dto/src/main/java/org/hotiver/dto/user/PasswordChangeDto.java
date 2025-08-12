package org.hotiver.dto.user;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PasswordChangeDto {
    private String password;
    private String code;
}
