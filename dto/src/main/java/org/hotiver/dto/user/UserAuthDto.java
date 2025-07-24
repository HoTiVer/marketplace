package org.hotiver.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAuthDto {
    String email;
    String displayName;
    String password;
}
