package org.hotiver.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeVerifyDto {
    String email;
    String code;
}
