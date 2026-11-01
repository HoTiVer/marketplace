package org.hotiver.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PasswordChangeDto {
    @NotBlank
    private String oldPassword;
    @NotBlank
    @Pattern(
            regexp = "^[A-Za-z0-9!@#$%^&*()_+=\\-]+$",
            message = "Password can contain only English letters, digits and allowed symbols"
    )

    private String newPassword;

    private String code;
}
