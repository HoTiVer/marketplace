package org.hotiver.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PasswordChangeDto {
    @NotBlank
    private String oldPassword;
    @NotBlank
    private String newPassword;

    private String code;
}
