package org.hotiver.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeVerifyDto {
    @NotBlank
    @Size(min = 1, max = 6)
    String code;
}
