package org.hotiver.common;

import java.util.ArrayList;
import java.util.List;

public enum OrderStatus {
    CREATED,
    CONFIRMED,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED,
    RETURNED,
    COMPLETED;

    public boolean canChangeTo(OrderStatus newStatus) {
        return switch (this) {
            case CREATED ->
                    newStatus == CONFIRMED || newStatus == CANCELLED;
            case CONFIRMED ->
                    newStatus == IN_TRANSIT || newStatus == CANCELLED;
            case IN_TRANSIT ->
                    newStatus == DELIVERED || newStatus == RETURNED;
            case DELIVERED ->
                    newStatus == COMPLETED || newStatus == RETURNED;
            default -> false;
        };
    }
}
