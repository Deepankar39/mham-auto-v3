package com.mhamauto.checkout;

/** A business-rule conflict, not a database outage. */
public final class CheckoutConflict extends RuntimeException {
    public CheckoutConflict(String message){super(message);}
}
