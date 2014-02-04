package com.tesco.services.core;

public interface PriceVisitable {
    public void accept(ProductPriceVisitor visitor);
}
