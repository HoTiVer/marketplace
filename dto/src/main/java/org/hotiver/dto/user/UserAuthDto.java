package org.hotiver.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAuthDto {
    @NotBlank(message = "Email is required")
    String email;

    @NotBlank(message = "Password is required")
    String password;

    String displayName;

    public UserAuthDto() {

    }

    public UserAuthDto(String email, String password, String displayName) {
        this.email = email;
        this.password = password;
        this.displayName = displayName;
    }

    public UserAuthDto(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
