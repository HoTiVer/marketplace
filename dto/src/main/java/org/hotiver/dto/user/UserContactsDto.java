package org.hotiver.dto.user;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserContactsDto {
    @Email
    String email;
}
