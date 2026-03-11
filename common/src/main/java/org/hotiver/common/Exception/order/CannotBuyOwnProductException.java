package org.hotiver.common.Exception.order;

public class CannotBuyOwnProductException extends RuntimeException {
    public CannotBuyOwnProductException(String message) {
        super(message);
    }
}
