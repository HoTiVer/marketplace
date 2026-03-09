package org.hotiver.dto.product;

import java.math.BigDecimal;
import java.util.Map;

public interface ProductProjection {
    Long getId();
    String getName();
    BigDecimal getPrice();
    String getDescription();
    String getCategoryName();
    String getImageUrl();
}
