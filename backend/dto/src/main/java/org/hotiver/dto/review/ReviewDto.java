package org.hotiver.dto.review;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDto {

    @NotNull
    @Max(5)
    @Min(1)
    private Integer rating;

    @Size(max=500)
    private String comment;

}
