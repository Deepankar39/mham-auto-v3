package com.mhamauto.cart;

/** Expected user-facing state conflict: product inactive or requested quantity exceeds source count. */
public final class CartConflict extends RuntimeException {
    public CartConflict(String message){super(message);}
}
