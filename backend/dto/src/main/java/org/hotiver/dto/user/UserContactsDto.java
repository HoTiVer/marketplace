package org.hotiver.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserContactsDto {
    @Email(message = "Incorrect email format")
    @NotBlank(message = "Email must be assigned")
    String email;
}
