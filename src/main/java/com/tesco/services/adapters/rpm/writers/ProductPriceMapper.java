package com.tesco.services.adapters.rpm.writers;

import com.tesco.services.core.Product;
import com.tesco.services.core.ProductVariant;
import com.tesco.services.core.SaleInfo;
import com.tesco.services.repositories.ProductPriceRepository;

import java.util.Map;

public class ProductPriceMapper {
    private ProductPriceRepository productPriceRepository;

    public ProductPriceMapper(ProductPriceRepository productPriceRepository) {
        this.productPriceRepository = productPriceRepository;
    }

    public Product mapPriceZonePrice(Map<String, String> headerToValueMap) {
        return mapToProduct(headerToValueMap, CSVHeaders.Price.PRICE_ZONE_ID, CSVHeaders.Price.PRICE_ZONE_PRICE);
    }

    public Product mapPromoZonePrice(Map<String, String> headerToValueMap) {
        return mapToProduct(headerToValueMap, CSVHeaders.Price.PROMO_ZONE_ID, CSVHeaders.Price.PROMO_ZONE_PRICE);
    }

    private Product mapToProduct(Map<String, String> headerToValueMap, String zoneIdHeader, String priceHeader) {
        String tpnb = headerToValueMap.get(CSVHeaders.Price.TPNB).split("-")[0]; //TODO: Remove the splitting logic once TPNC is given in the CSV extracts
        Product product = productPriceRepository.getByTPNB(tpnb).or(new Product(tpnb));

        String tpnc = headerToValueMap.get(CSVHeaders.Price.TPNB); // Todo: to be changed to tpnc when we get tpnc
        ProductVariant productVariant = product.getProductVariantByTPNC(tpnc);

        if (productVariant == null) {
            productVariant = new ProductVariant(tpnc);
            product.addProductVariant(productVariant);
        }

        final int zoneId = Integer.parseInt(headerToValueMap.get(zoneIdHeader));
        productVariant.addSaleInfo(new SaleInfo(zoneId, headerToValueMap.get(priceHeader)));

        return product;
    }
}
