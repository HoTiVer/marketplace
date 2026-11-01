package org.hotiver.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank
    @Pattern(
            regexp = "^[A-Za-z0-9!@#$%^&*()_+=\\-]+$",
            message = "Password can contain only English letters, digits and allowed symbols"
    )
    private String newPassword;

    @NotBlank
    private String resetToken;
}
