package org.hotiver.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hotiver.dto.validation.RegisterRequest.DuplicatedEmailConstraint;
import org.hotiver.dto.validation.RegisterRequest.StrongPassword;

@Getter
@Setter
public class RegisterRequest {
    @Email
    @NotBlank(message = "Email is required")
    @DuplicatedEmailConstraint
    String email;

    @StrongPassword
    @NotBlank(message = "Password is required")
    String password;

    @NotBlank(message = "Display name is required")
    String displayName;

    public RegisterRequest(String email, String password, String displayName) {
        this.email = email;
        this.password = password;
        this.displayName = displayName;
    }
}
