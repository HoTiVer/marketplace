package org.hotiver.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAuthDto {
    @NotBlank(message = "Email is required")
    String email;

    String displayName;

    @NotBlank(message = "Password is required")
    String password;
}
