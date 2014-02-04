package com.tesco.services.core;

public interface ProductPriceVisitor {
    public void visit(Product product);
    public void visit(ProductVariant productVariant);
}
