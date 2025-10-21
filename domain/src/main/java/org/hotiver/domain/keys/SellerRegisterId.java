package org.hotiver.domain.keys;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class SellerRegisterId implements Serializable {
    private Long userId;
    private Date requestDate;
}
