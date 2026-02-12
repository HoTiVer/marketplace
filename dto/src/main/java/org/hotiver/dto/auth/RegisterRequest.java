package org.hotiver.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @Email
    @NotBlank(message = "Email is required")
    //TODO @UniqueEmail
    String email;

    //TODO @StrongPassword
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
